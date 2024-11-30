package com.buaa01.illumineer_backend.entity.singleton;

public class FidnumSingleton {

    // 使用 volatile 确保线程安全
    private static volatile FidnumSingleton instance = null;

    // fidnum 初值为 0
    private int fidnum = -1;

    // 私有构造函数，防止外部实例化
    private FidnumSingleton() {
    }

    // 获取唯一实例的公共方法
    public static FidnumSingleton getInstance() {
        if (instance == null) {
            synchronized (FidnumSingleton.class) {
                if (instance == null) {
                    instance = new FidnumSingleton();
                }
            }
        }
        return instance;
    }

    // 获取 fidnum 的方法
    public int getFidnum() {
        return fidnum;
    }

    // 修改 fidnum 的方法
    public void setFidnum(int fidnum) {
        this.fidnum = fidnum;
    }

    // 使fidnum+1并返回
    public Integer addFidnum() {
        fidnum++;
        return fidnum;
    }
}
