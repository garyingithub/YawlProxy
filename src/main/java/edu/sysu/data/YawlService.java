package edu.sysu.data;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;

/**
 * Created by gary on 16-7-31.
 */
@Entity
@Table(name = "YawlServices")
public class YawlService {

    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment",strategy = "increment")
    private long id;

    @NaturalId
    private String uri;

    private String name;

    @org.hibernate.annotations.Type( type = "text" )
    private String document;

    private String password;

    @ManyToOne

    private Tenant tenant;

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }




    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }



    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public String getTenantIdAndName(){
        return this.tenant.getTenantId()+":"+name;
    }




    public YawlService(){}


}
