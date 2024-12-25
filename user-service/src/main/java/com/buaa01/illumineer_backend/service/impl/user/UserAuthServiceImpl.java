package com.buaa01.illumineer_backend.service.impl.user;

import com.buaa01.illumineer_backend.entity.CustomResponse;
import com.buaa01.illumineer_backend.entity.User;
import com.buaa01.illumineer_backend.service.user.UserAuthService;
import com.buaa01.illumineer_backend.service.utils.CurrentUser;
import com.buaa01.illumineer_backend.tool.RedisTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAuthServiceImpl implements UserAuthService {

    @Autowired
    private CurrentUser currentUser;
    @Autowired
    private RedisTool redisTool;



    /**
     * 按照无序集合查询
     * 同时维护两个set1
     * paperBelonged : 拥有该文章的作者的uid集合
     * property : 某个作者拥有文章的pid集合
     * **/
    @Override
    public CustomResponse claim(Integer add , List<Long> pids){
        CustomResponse customResponse = new CustomResponse();
        User user = currentUser.getUser();
        //初始化

        customResponse.setCode(200);
        customResponse.setMessage("论文认领成功");
        String fidKey = "property:" + user.getUid();
        String adoptionKey = "adoption :" + user.getName();
        //添加文章
        if(add==1) {
            for (Long pid : pids) {
               if (redisTool.isSetMember(fidKey, pid)) {
                }
                //收藏论文
                else {
                    redisTool.addSetMember(fidKey, pid);
                    redisTool.deleteSetMember(adoptionKey, pid);

                    //在文章的序列中添加作者
                    String authKey = "paperBelonged:" + pid;
                    if(!redisTool.isExist(authKey)){
                        //FIXME: 这里可以通过这种方式创建key吗？
                        System.out.println("不存在该论文 认领者的 集合");
                        redisTool.addSetMember(authKey,currentUser.getUser().getUid());
                    }else if(redisTool.isSetMember(authKey,currentUser.getUser().getUid())){
                        customResponse.setCode(500);
                        customResponse.setMessage("用户已存在于该文章的认领者集合中");
                    }else{
                        redisTool.addSetMember(authKey,currentUser.getUser().getUid());
                    }
                }
            }
        }else {
            //删除操作
            for (Long pid : pids) {
                if (!redisTool.isExist(fidKey)) {
                    customResponse.setCode(500);
                    customResponse.setMessage("Redis中该用户实名下的论文集合未创建，可能未在实名过程中调用创建函数");
                }
                //fid下没有有该论文
                else if (!redisTool.isSetMember(fidKey, pid)) {
                    customResponse.setCode(500);
                    customResponse.setMessage("尝试删除本不存在于集合中的论文");
                }
                //收藏论文
                else {
                    //可以使用这个函数吗？
                    redisTool.deleteSetMember(fidKey, pid);
                    //在文章的序列中删除作者
                    String authKey = "paperBelonged:" + pid;
                    if(!redisTool.isExist(authKey)){
                        //FIXME: 这里可以通过这种方式创建key吗？
                        System.out.println("不存在该论文 认领者的 集合");
                        redisTool.addSetMember(authKey,currentUser.getUser().getUid());
                    }else if(!redisTool.isSetMember(authKey,currentUser.getUser().getUid())){
                        customResponse.setCode(500);
                        customResponse.setMessage("尝试删除本不存在于改论文认领者集合中的用户");
                    }else{
                        redisTool.addSetMember(authKey,currentUser.getUser().getUid());
                    }
                }
            }
        }
        return customResponse;
    }

    @Override
    public CustomResponse authentation(String name,String Institution,Integer gender){
        CustomResponse customResponse = new CustomResponse();
        User user = currentUser.getUser();
        user.setName(name);
        user.setInstitution(Institution);
        user.setGender(gender);
        customResponse.setCode(200);
        customResponse.setMessage("Authentication Update Success");
        return customResponse;
    }
}
