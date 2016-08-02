package edu.sysu.util;

import edu.sysu.data.Engine;
import edu.sysu.data.Specification;
import edu.sysu.data.Tenant;
import org.apache.commons.codec.binary.Base64;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.schema.YSchemaVersion;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.unmarshal.YMetaData;
import org.yawlfoundation.yawl.util.*;

import javax.inject.Singleton;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by gary on 16-7-31.
 */

public class YawlUtil {

    public String failureMessage(String msg) {
        return StringUtil.wrap(StringUtil.wrap(msg, "reason"), "failure");
    }

    public YawlUtil(HibernateUtil hibernateUtil) {
        this.hibernateUtil = hibernateUtil;
    }

    public String successMessage(String msg) {
        return StringUtil.wrap(msg, "success");
    }

    public YSpecificationID getDescriptors(String specxml) {
        YSpecificationID descriptors = null;
        XNode specNode = new XNodeParser().parse(specxml);
        if (specNode != null) {
            YSchemaVersion schemaVersion = YSchemaVersion.fromString(
                    specNode.getAttributeValue("version"));
            XNode specification = specNode.getChild("specification");
            if (specification != null) {
                String uri = specification.getAttributeValue("uri");
                String version = "0.1";
                String uid = null;
                if (!(schemaVersion == null || schemaVersion.isBetaVersion())) {
                    XNode metadata = specification.getChild("metaData");
                    version = metadata.getChildText("version");
                    uid = metadata.getChildText("identifier");
                }
                descriptors = new YSpecificationID(uid, version, uri);
            } else return null;
        } else return null;

        return descriptors;
    }

    public String connectToEngine(Engine engine, String userId, String password) throws UnsupportedEncodingException, NoSuchAlgorithmException {

        String destination = engine.getUrl() + "/ib";

        RequestForwarder fw = new RequestForwarder();
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("action", "connect");
        parameters.put("userid", userId);
        parameters.put("password", encrypt(password));


        try {
            String result = fw.forwardRequest(destination, parameters);


            if (result.startsWith("<")) {
                return result.substring(10, 10 + 36);
            } else {
                return result;
            }

        } catch (Exception e) {
            return "";
        }
    }

    public String connectToEngineAsAdmin(Engine engine) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        return connectToEngine(engine, "admin", "YAWL");
    }

    public String connectToEngineAsDefaultWorklist(Engine engine) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        return connectToEngine(engine, "DefaultWorklist", "resource");
    }

    public synchronized String encrypt(String text)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA");
        md.update(text.getBytes("UTF-8"));
        byte raw[] = md.digest();
        return new Base64(-1).encodeToString(raw);            // -1 means no line breaks
    }

    public String getBuildProperties() {
        String content = "<buildproperties> " +
                "  <OSVersion>10.11.4</OSVersion>" +
                "  <BuiltBy>adamsmj</BuiltBy>" +
                "  <BuildNumber>1,401</BuildNumber>" +
                "  <OS>Mac OS X</OS>" +
                "  <JavaVersion>1.8.0_45</JavaVersion>" +
                "  <Version>4.1</Version>" +
                "  <BuildDate>2016/05/10 12:08</BuildDate>" +
                "  </buildproperties>";
        return content;
    }

    HibernateUtil hibernateUtil;

    public String loadSpecification(String specXML, Tenant tenant) {

        YVerificationHandler verificationHandler = new YVerificationHandler();
        List<YSpecification> newSpecifications = null;
        try {
            newSpecifications = YMarshal.unmarshalSpecifications(specXML);
        } catch (YSyntaxException e) {
            e.printStackTrace();
        }

        if (newSpecifications != null) {
            for (YSpecification specification : newSpecifications) {
                specification.verify(verificationHandler);
                if (verificationHandler.hasErrors()) {
                    String errDetail = specification.getSchemaVersion().isBetaVersion() ?
                            "URI: " + specification.getURI() : "UID: " + specification.getID();
                    errDetail += "- Version: " + specification.getSpecVersion();
                    return ("There is a specification with an identical id to ["
                            + errDetail + "] already loaded into the engine.");
                } else {
                    Specification spec = new Specification();
                    spec.setIdAndVersion(specification.getID() +":"+ specification.getSpecVersion());
                    spec.setXML(YMarshal.marshal(specification));
                    spec.setTenant(tenant);
                    spec.setSpecificationUri(specification.getURI());
                    tenant.addSpecification(spec);
                    hibernateUtil.updateObject(tenant);
                }
            }
        }
        return "";

    }

    public String getDataForSpecifications(Set<YSpecification> specSet) {
        StringBuilder specs = new StringBuilder();
        for (YSpecification spec : specSet) {
            specs.append("<specificationData>");
            specs.append(StringUtil.wrap(spec.getURI(), "uri"));

            if (spec.getID() != null) {
                specs.append(StringUtil.wrap(spec.getID(), "id"));
            }
            if (spec.getName() != null) {
                specs.append(StringUtil.wrap(spec.getName(), "name"));
            }
            if (spec.getDocumentation() != null) {
                specs.append(StringUtil.wrap(spec.getDocumentation(), "documentation"));
            }

            Iterator inputParams = spec.getRootNet().getInputParameters().values().iterator();
            if (inputParams.hasNext()) {
                specs.append("<params>");
                while (inputParams.hasNext()) {
                    YParameter inputParam = (YParameter) inputParams.next();
                    specs.append(inputParam.toSummaryXML());
                }
                specs.append("</params>");
            }
            specs.append(StringUtil.wrap(spec.getRootNet().getID(), "rootNetID"));
            specs.append(StringUtil.wrap(spec.getSchemaVersion().toString(), "version"));
            specs.append(StringUtil.wrap(spec.getSpecVersion(), "specversion"));
            specs.append(StringUtil.wrap("loaded", "status"));
            YMetaData metadata = spec.getMetaData();
            if (metadata != null) {
                specs.append(StringUtil.wrap(metadata.getTitle(), "metaTitle"));
                List<String> creators = metadata.getCreators();
                if (creators != null) {
                    specs.append("<authors>");
                    for (String author : creators) {
                        specs.append(StringUtil.wrap(author, "author"));
                    }
                    specs.append("</authors>");
                }
            }
            String gateway = spec.getRootNet().getExternalDataGateway();
            if (gateway != null) {
                specs.append(StringUtil.wrap(gateway, "externalDataGateway"));
            }
            specs.append("</specificationData>");
        }
        return specs.toString();
    }
}
