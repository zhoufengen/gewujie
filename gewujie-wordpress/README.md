#安装部署启动

mkdir gewujie-caddy-data  gewujie-redis-data  mysql-init  gewujie-caddy-config  gewujie-db-data   gewujie-wordpress

sudo chmod -R 777 mysql-init gewujie-redis-data   gewujie-db-data    

docker-compose up -d
docker-compose logs -f
docker-compose down

#初始化工作
数据库不要文件夹，不要放在虚拟机映射的文件夹下

#启用redis插件：Redis Object Cache
经过上面步骤后，WordPress 已经可以运行，但是 Redis 对象缓存还未启用，这时需要去当前目录下 WordPress 文件目录中的 config.php 文件中，添加下列内容：

cd ./gewujie-wordpress
sudo vim wp-config.php
define('WP_CACHE', true);
define('WP_REDIS_HOST', 'gewujie-redis');
define('WP_REDIS_PORT', 6379);
define('WP_REDIS_PASSWORD', 'Aa686766');
define('WP_REDIS_TIMEOUT', 3);
define('WP_REDIS_READ_TIMEOUT', 3);


然后在 WordPress 后台中安装 Redis Object Cache 插件并启用即可。

然后点击启用缓存，修改生成的这个文件：
sudo vim gewujie-wordpress/wp-content/object-cache.php
  protected function build_parameters() {
        $parameters = [
            'scheme' => 'tcp',
            'host' => 'gewujie-redis',  #这里改下
            'port' => 6379,
            'database' => 0,
            'password' => 'Aa686766',   #这里改下
            'timeout' => 1,
            'read_timeout' => 1,
            'retry_interval' => null,
            'persistent' => false,
        ];


#登录wordpress更新才有中文界面
sudo vim gewujie-wordpress/wp-config.php

define( 'WPLANG', 'zh_CN');
