package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "permissions")
public class Permission extends Model {
    @ManyToOne
    public Directory directory;
    public String userName;
    public String permission;

    @Transient
    public String displayName;
    @Transient
    public Integer userId;

    public String toRule() {
        return ("all".equals(userName) ? "*" : userName) + " = " + permission;
    }
}
