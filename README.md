
<p align="center">
	<img src="vx_images/logo.png" />
</p>
<p align="center">
	<strong>适合互联网企业使用的开源智慧商场系统</strong>
</p>
<p align="center">
	👉 <a href="https://www.scmintu.com/">https://www.scmintu.com/</a> 👈
</p>

<p align="center">
	<a target="_blank" href="https://spring.io/projects/spring-boot">
		<img src="https://img.shields.io/badge/spring%20boot-2.4.5-yellowgreen" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html">
		<img src="https://img.shields.io/badge/JDK-8-green.svg" />
	</a>
	<a target="_blank" href="http://www.gnu.org/licenses/lgpl.html">
		<img src="https://img.shields.io/badge/license-LGPL--3.0-blue" />
	</a>
</p>


-------------------------------------------------------------------------------

## 📚 项目介绍

SmartMall是一套适合超市、购物中心、个体店铺使用的开源智慧商场系统，系统个性化营销、数字化运营，让商超、购物中心效果最大化，市场落地案列多成熟稳定。
此项目是后端工程（数据库没开源，如果需要商用、演示或者试用请联系我们）

- 前端工程：[https://gitee.com/scmt1/SmartMallUI](https://gitee.com/scmt1/SmartMallUI "智慧商场系统（前端端）")


## 🍎 项目特点

* 采用微应用架构满足可扩展功能
* 使用`spring boot`框架作为后端框架，便于维护
* 接口请求和响应数据采用签名机制，保证交易安全可靠
* 管理平台操作界面简洁、易用
* 使用`spring security`实现权限管理
* 前后端分离架构，方便二次开发

## 🍟 项目体验
- 智慧商场系统：[https://scmintu.com/](https://scmintu.com/ "（公司官网查看）智慧商场系统")

## 🥞 系统架构

> 开源智慧商场系统架构图
![系统架构设计 (2)](vx_images/xtjg.png)


> 核心技术栈

| 软件名称  | 描述 | 版本
|---|---|---
|Jdk | Java环境 | 1.8
|Spring Boot | 开发框架 | 2.4.5
|Redis | 分布式缓存 | 3.2.8 或 高版本
|MySQL | 数据库 | 5.7.X 或 8.0 高版本
|[Iview Ui](http://iview.talkingdata.com/) | iview Vue框架，前端开发使用 | 4.7.0
|[MyBatis-Plus](https://mp.baomidou.com/) | MyBatis增强工具 | 3.4.2
|[Hutool](https://www.hutool.cn/) | Java工具类库 | 5.6.6

> 项目结构（前后端分离了的，可以分明查看）

```lua

SmartMall

├──mall4j-bbc -- 系统后端
├──mall4uni-bbc -- 手机端
├──mall4vp-bbc -- 运营前端
├──mall4vs-bbc -- 商家前端
├──mall-cashier -- 收银台
├──mall-xxl-job-- 定时任务(含前后端)
└── vx_images -- 项目截图
```



## 🍿 功能介绍
1.运营平台（含店铺管理、热搜管理、公告管理、物流管理、商品管理、订单管理、会员管理、积分管理、营销管理、购物卡、智慧停车、数据报表、财务管理、页面装修）；
2.商家店铺平台（含店铺管理、商品管理、商品库存管理、营销管理、订单管理、数据报表、店铺装修、店铺公告）；
3.商家店铺移动端（含店铺首页、订单管理、商品管理、商品发布、商品概况）；
4.收银台（含订单管理、核销、会员积分、商品开单、礼品兑换）；
5.消费群众端（含会员中心、线上商城、停车服务、拼团秒杀活动、优惠卷、会员积分、积分商城、积分抽奖）
6.智慧物业：街区安防能力智能化提升、视频监控、明厨亮灶、电气安全监控、智慧物业、智慧停车。

> 功能结构图
![系统功能模块图 (1)](vx_images/1.png)

## 🍯 系统截图

`以下截图是从实际已完成功能界面截取,截图时间为：2024-08-31 15:59`
### 1、运营平台
#### 1.1首页
![](vx_images/2.png)
#### 1.2订单管理
![](vx_images/3.png)
#### 1.3商品管理
![](vx_images/4.png)
#### 1.4会员管理
![](vx_images/5.png)
#### 1.5店铺管理
![](vx_images/6.png)
#### 1.6营销管理
![](vx_images/7.png)
![](vx_images/8.png)
![](vx_images/9.png)
![](vx_images/10.png)
![](vx_images/11.png)
![](vx_images/3.png)
#### 1.7数据报表
![](vx_images/12.png)
#### 1.8财务管理
![](vx_images/13.png)
#### 1.9装修管理
![](vx_images/14.png)
![](vx_images/15.png)
#### 1.10公告管理
![](vx_images/16.png)
### 2、商家店铺端
#### 2.1订单管理
![](vx_images/17.png)
#### 2.2商品管理
![](vx_images/18.png)
![](vx_images/19.png)
#### 2.3库存管理
![](vx_images/20.png)
#### 2.4.营销管理
![](vx_images/21.png)
![](vx_images/22.png)
![](vx_images/23.png)
#### 2.5数据报表
![](vx_images/24.png)
![](vx_images/25.png)
#### 2.6店铺装修
![](vx_images/26.png)
### 3、商家店铺移动端
#### 3.1店铺首页
![](vx_images/27.jpg)
#### 3.2订单管理
![](vx_images/28.png)![](vx_images/29.jpg)
#### 3.3商品管理
![](vx_images/30.png)![](vx_images/31.png)

![](vx_images/32.png)
### 4、收银台
#### 4.1订单管理
![](vx_images/33.png)
#### 4.2核销
![](vx_images/34.png)
#### 4.3开单
![](vx_images/35.png)
#### 4.4会员积分
![](vx_images/36.png)
### 5、消费群众端
#### 5.1首页
![](vx_images/37.png)
#### 5.2会员中心
![](vx_images/38.png)![](vx_images/39.png)
![](vx_images/40.png)
#### 5.3线上商城
![](vx_images/41.png)![](vx_images/42.png)
#### 5.4优惠卷
![](vx_images/43.png)![](vx_images/44.png)
#### 5.5停车服务
![](vx_images/45.png)
#### 5.6拼团、秒杀活动
![](vx_images/46.png)
#### 5.7自助积分
![](vx_images/47.png)
#### 5.8幸运抽奖
![](vx_images/48.png)
### 6、智慧物业
![](vx_images/49.png)

## 🥪开源版使用须知
***
* 需标注"代码来源于四川民图科技开源项目"后即可免费自用运营
* 前端运营时展示的内容不得使用四川民图科技相关信息
* 允许用于个人学习、教学案例
* 开源版不得直接倒卖源码
* 禁止将本项目的代码和资源进行任何形式的出售，产生的一切任何后果责任由侵权者自负

## 🥪商业合作
***
*  如果你想使用功能更完善的智慧商城系统，请联系电话：400-855-2332 或者 微信 wxid_1uhin30bp6xv12
*  如果您想基于智慧商城系统进行定制开发，我们提供有偿定制服务支持！
*  其他合作模式不限，欢迎来撩！
*  联系我们（商务请联系电话：400-855-2332 或者微信  wxid_1uhin30bp6xv12）

## 🥪 关于我们
***
* 公司名称：四川民图科技有限公司
* 地址：成都市金牛区北三环路一段221号
* 电话：400-855-2332
* 电话：400-855-2332
* 业务合作：scmtkj@163.com
* 公司主页：https://www.scmintu.com/
## 🥪 微信
微信联系方式：微信号 wxid_1uhin30bp6xv12

![微信](vx_images/vx.jpg)


