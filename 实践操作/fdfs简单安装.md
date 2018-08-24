java -cp  fdfs-client-java.jar org.csource.fastdfs.test.TestClient fdfs_client.conf data.txt

1.首先准备一台centos 7 系统
笔者为：192.168.2.163   主机名 kb163

2.编译环境准备
[root@kb163 src]# yum install git gcc gcc-c++ make automake autoconf libtool pcre pcre-devel zlib zlib-devel openssl-devel -y

3.目录准备
    所有安装包位置   /usr/local/src
    tracker跟踪服务器数据  /fastdfs/tracker
    storage存储服务器数据  /fastdfs/storage
[root@kb163 src]# mkdir -p /fastdfs/tracker  
[root@kb163 src]# mkdir -p /fastdfs/storage  

4.安装libfatscommon
[root@kb163 src]# cd /usr/local/src
[root@kb163 src]# git clone https://github.com/happyfish100/libfastcommon.git --depth 1
[root@kb163 src]# cd libfastcommon/
[root@kb163 src]# ./make.sh && ./make.sh install

5.安装FastDFS
[root@kb163 src]# cd /usr/local/src
[root@kb163 src]# git clone https://github.com/happyfish100/fastdfs.git --depth 1
[root@kb163 src]# cd fastdfs/
[root@kb163 src]# ./make.sh && ./make.sh install
#配置文件准备
cp /etc/fdfs/tracker.conf.sample /etc/fdfs/tracker.conf
cp /etc/fdfs/storage.conf.sample /etc/fdfs/storage.conf

#客户端文件，测试用
cp /etc/fdfs/client.conf.sample /etc/fdfs/client.conf 
#供nginx访问使用
cp /usr/local/src/fastdfs/conf/http.conf /etc/fdfs/
#供nginx访问使用
cp /usr/local/src/fastdfs/conf/mime.types /etc/fdfs/ 

6.安装fastdfs-nginx-module
[root@kb163 src]# cd /usr/local/src
git clone https://github.com/happyfish100/fastdfs-nginx-module.git --depth 1
cp /usr/local/src/fastdfs-nginx-module/src/mod_fastdfs.conf /etc/fdfs

7.安装nginx
[root@kb163 src]# cd /usr/local/src
wget http://nginx.org/download/nginx-1.12.2.tar.gz
tar -zxvf nginx-1.12.2.tar.gz
cd nginx-1.12.2/
#添加fastdfs-nginx-module模块
./configure --add-module=/usr/local/src/fastdfs-nginx-module/src/
make && make install
#j检查版本 
/usr/local/nginx/sbin/nginx -v


8.单机部署

 A.tracker配置
    vim /etc/fdfs/tracker.conf
    #需要修改的内容如下
    port=22122  # tracker服务器端口（默认22122,一般不修改）
    base_path=/fastdfs/tracker  # 存储日志和数据的根目录
    #保存后启动
    /etc/init.d/fdfs_trackerd start #启动tracker服务
    chkconfig fdfs_trackerd on #自启动tracker服务
    #查看进程
    netstat -apn |grep fdfs
    # tcp        0      0 0.0.0.0:22122           0.0.0.0:*               LISTEN      35687/fdfs_trackerd 

 B.storage配置
    vim /etc/fdfs/storage.conf
    #需要修改的内容如下
    port=23000  # storage服务端口（默认23000,一般不修改）
    base_path=/fastdfs/storage  # 数据和日志文件存储根目录
    store_path0=/fastdfs/storage  # 第一个存储目录
    tracker_server=192.168.2.163:22122  # tracker服务器IP和端口
    http.server_port=8888  # http访问文件的端口(默认8888,看情况修改,和nginx中保持一致)
    #保存后启动
    /etc/init.d/fdfs_storaged start #启动storage服务
    chkconfig fdfs_storaged on #自启动storage服务
     #查看进程
    netstat -apn |grep fdfs
    #tcp        0      0 0.0.0.0:22122           0.0.0.0:*               LISTEN      35687/fdfs_trackerd 

 C.client测试
    vim /etc/fdfs/client.conf
    #需要修改的内容如下
    base_path=/fastdfs/tracker
    tracker_server=192.168.1.163:22122    #tracker IP地址
    #保存后测试,返回ID表示成功 eg:group1/M00/00/00/wKgAQ1pysxmAaqhAAA76tz-dVgg.tar.gz
    fdfs_upload_file /etc/fdfs/client.conf /usr/local/src/nginx-1.12.2.tar.gz