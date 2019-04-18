服务异常日志统一处理添加流程

一：pom文件添加
        <dependency>
            <groupId>com.ydcloud</groupId>
            <artifactId>log-service-skeleton</artifactId>
            <version>${log.service.skeleton.version}</version>
		</dependency>

二：定义获取用户详情和插入日志的fegin UaaUserServiceFeign，UaaLogServiceFeign 
实例代码如下：
/**
 * @ClassName: UaaUserService
 * @Description: 用户远程处理接口
 * @author: wangzhaowen:ydyt16
 * @UpdateDate: 2019/04/13 17:45:24 
 */
@FeignClient("${user.feign.name}")
@RequestMapping("/service/")
public interface UaaUserServiceFeign {

    /**
     * 查询用户详情
     * @param userId
     * @throws ServiceException
     */
    @GetMapping("/user/detail/byUserId")
    UaaUser detailUaaUser(@RequestParam("userId") Long userId)throws ServiceException;


     /**
     * 
     * @Title: getAllOperation
     * @Description: 获取全部权限基础信息
     * @return
     * @throws ServiceException
     * @return: List<UaaOperation>
     */
    @GetMapping("/operation/allUOperation/list")
    public List<UaaOperation> getAllOperation()throws ServiceException;
}


/**
 * @ClassName: UaaLogServiceFeign
 * @Description: 日志远程调用处理接口
 * @author: wangzhaowen:ydyt16
 * @UpdateDate: 2019/04/13 20:51:19 
 */
@FeignClient("${log.feign.name}")
@RequestMapping("/service/")
public interface UaaLogServiceFeign {

	/**
	 * 添加操作日志
	 * 
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/log/operation/insert_info")
	Long insertOperationLog(@RequestBody UaaOperationLogWithBLOBs dataLog) throws ServiceException;



	/**
	 * 
	 * @Title: getAllLogExtend
	 * @Description: 获取日志扩展信息
	 * @return
	 * @return: List<UaaOperationLogExtend>
	 */
	@GetMapping("/lists/operation/byMap")
	List<UaaOperationLogExtend> getAllLogExtend();

}

三：实现日志处理类com.yst.log.service.AbstractLogService 

package com.yst.user.service.impl;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;
import com.hcloud.entity.DataEntity;
import com.hcloud.exception.ServiceException;
import com.yst.log.domain.UaaOperationLogExtend;
import com.yst.log.domain.UaaOperationLogWithBLOBs;
import com.yst.log.service.AbstractLogService;
import com.yst.user.domain.UaaOperation;
import com.yst.user.feign.log.UaaLogServiceFeign;
import com.yst.user.service.UaaOperationService;
import com.yst.user.service.UaaUserService;
/**
 * @ClassName: LogServiceImpl
 * @Description: 具体日志数据封装实现类
 * @author: wangzhaowen:ydyt16
 * @UpdateDate: 2019/04/17 12:24:23 
 */
@Service("LogServiceImpl")
public class LogServiceImpl  extends AbstractLogService{

	/**
	 * @fieldName: tokenStore
	 * @fieldType: TokenStore
	 * @Description: 登录用户token处理对象
	 */
	@Autowired
	private TokenStore tokenStore;
 
	/**
	 * @fieldName: uaaUserServiceFeign
	 * @fieldType: UaaUserServiceFeign
	 * @Description: 用户查询服务接口
	 */
	@Autowired
	private UaaUserService uaaUserServiceFeign;
 
	/**
	 * @fieldName: uaaLogServiceFeign
	 * @fieldType: UaaLogServiceFeign
	 * @Description: 远程日志处理接口
	 */
	@Lazy    //防止循序注入
	@Autowired
	private UaaLogServiceFeign uaaLogServiceFeign;
	
	
	@Autowired
	private UaaOperationService uaaOperationService;

	/* (non Javadoc)
	 * @Title: save
	 * @Description: 日志插入具体实现
	 * @param Log 日志对象
	 * @throws ServiceException
	 * @see com.hcloud.service.LogServvice#save(com.hcloud.entity.DataEntity)
	 */
	@Override
	public void save(DataEntity Log) throws ServiceException {
		uaaLogServiceFeign.insertOperationLog((UaaOperationLogWithBLOBs)Log);
	}
 
	/* 
	 * @Title: getUser
	 * @Description:  获取登录用户信息
	 * @param request
	 * @return
	 * @see com.yst.log.service.AbstractLogService#getUser(javax.servlet.http.HttpServletRequest)
	 */
	public DataEntity getUser(HttpServletRequest request) {
		Long userId = getUserId(tokenStore, request);
		if (userId != null) {
			return uaaUserServiceFeign.detailUaaUser(userId);
		}
		return null;
	}
 
	@Override
	protected List<UaaOperationLogExtend> getAllLogExtend() {
		return uaaLogServiceFeign.getAllLogExtend();
	}

	@Override
	protected List<UaaOperation> getAllOperation() {
		return uaaOperationService.getAllOperation();
	}
 
}


四：注入bean：CommonExceptionAdvice 
代码如下：

/**
 * 
 * @Title: commonExceptionAdvice
 * @Description: 注入全局处理器
 * @param LogServvice  日志处理具体实现类
 * @return
 * @return: CommonExceptionAdvice
 */
@Bean
public CommonExceptionAdvice commonExceptionAdvice(LogServvice LogServvice) {
	return new CommonExceptionAdvice(LogServvice);
}


五：配置文件添加配置

log.feign.name=log-service  ##配置日志feign服务名
log.exclution.uri=list,get  ##过滤接口关键字  包含list，get的接口不做记录

六：启动类加注解
实例：

....
@ComponentScan(basePackages={"com.yst.user","com.yst.log.config"})   //com.yst.log.config为日志Aspect位置
....
public class UserApplication {

}