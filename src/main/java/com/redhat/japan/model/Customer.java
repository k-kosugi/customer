package com.redhat.japan.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "public.customer")
@NamedQueries(
        @NamedQuery(name = "Customer.findAll", query = "select a from Customer a")
)
public class Customer implements Serializable {
    private static final long serialVersionUID = -8183963609634214352L;

    @Id
    public long id;

    @Column(name = "ssn", length = 25)
    public String ssn;

    @Column(name = "name", length = 64)
    public String name;

}
