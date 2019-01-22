package guo.wifilistconnect.bean;

/**
 * Created by John on 2017/4/7.
 */

public class WifiBean implements Comparable<WifiBean> {
    private String wifiName;
    private String level;
    private String state;  //已连接  正在连接  未连接 三种状态
    private String capabilities;//加密方式
    private int imageId;
    private int ifVisibility;

    @Override
    public String toString() {
        return "WifiBean{" +
                "wifiName='" + wifiName + '\'' +
                ", level='" + level + '\'' +
                ", state='" + state + '\'' +
                ", capabilities='" + capabilities + '\'' +
                ", imageId='" + imageId +'\'' +
                ", ifVisibility='" + ifVisibility +'\'' +
                '}';
    }

    public String getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }

    public String getWifiName() {
        return wifiName;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }

    public int getImageId() {
        return imageId;
    }
    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public void setIfLock(int ifVisibility) {
        this.ifVisibility = ifVisibility;
    }
    public int getIfLock() {
        return ifVisibility;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public int compareTo(WifiBean o) {
        int level1 = Integer.parseInt(this.getLevel());
        int level2 = Integer.parseInt(o.getLevel());
        return level2 - level1;//sort stander
        //降序
        //return o.age - this.age;
        //升序
        // this.age - o.age;
    }
}
