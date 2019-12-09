package com.redhat.japan.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "public.address")
public class Address implements Serializable {
    private static final long serialVersionUID = 2141307916419525757L;

    @Id
    public long id;

    @Column(name = "street", length = 25)
    public String street;

    @Column(name = "zip", length = 10)
    public String zip;

    public long customer_id;

}
