package userAuth;

//user entity
//sql schema can be find in resources/sql/
public class User {
    private int userId;
    private String userName;
    private String userPwd;
    private String permission;   //admin, editor, viewer

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserPwd() { return this.userPwd; }
    public void setUserPwd(String userPwd) { this.userPwd = userPwd; }

    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }
}