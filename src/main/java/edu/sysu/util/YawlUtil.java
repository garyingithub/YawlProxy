package edu.sysu.util;

import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.unmarshal.YMetaData;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Some useful functions extracted from the yawllib
 * Created by gary on 16-7-31.
 */

public class YawlUtil {

    public static String failureMessage(String msg) {
        return StringUtil.wrap(StringUtil.wrap(msg, "reason"), "failure");
    }


    public static String successMessage(String msg) {
        return StringUtil.wrap(msg, "success");
    }


    public static String getBuildProperties() {
        return "<buildproperties> " +
                "  <OSVersion>10.11.4</OSVersion>" +
                "  <BuiltBy>adamsmj</BuiltBy>" +
                "  <BuildNumber>1,401</BuildNumber>" +
                "  <OS>Mac OS X</OS>" +
                "  <JavaVersion>1.8.0_45</JavaVersion>" +
                "  <Version>4.1</Version>" +
                "  <BuildDate>2016/05/10 12:08</BuildDate>" +
                "  </buildproperties>";
    }




    public static String getDataForSpecifications(Set<YSpecification> specSet) {
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
