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

    @ManyToOne
    private Tenant tenant;

    public YawlService(){}


}
