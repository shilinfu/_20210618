package com.shilinfu;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

public class HdfsClient {
    FileSystem fs = null;

    @Before
    public void init() throws Exception {
        // 构造一个配置参数对象，设置一个参数：我们要访问的hdfs的URI
        // 从而FileSystem.get()方法就知道应该是去构造一个访问hdfs文件系统的客户端，以及hdfs的访问地址
        // new Configuration();的时候，它就会去加载jar包中的hdfs-default.xml
        // 然后再加载classpath下的hdfs-site.xml
        Configuration conf = new Configuration();
        // 192.168.225.128  linfu1
        conf.set("fs.defaultFS", "hdfs://linfu1:9000");
        /**
         * 参数优先级： 1、客户端代码中设置的值 2、classpath下的用户自定义配置文件 3、然后是服务器的默认配置
         */
        conf.set("dfs.replication", "3");
        // 获取一个hdfs的访问客户端，根据参数，这个实例应该是DistributedFileSystem的实例
        // fs = FileSystem.get(conf);
        // 如果这样去获取，那conf里面就可以不要配"fs.defaultFS"参数，而且，这个客户端的身份标识已经是hadoop用户
        fs = FileSystem.get(new URI("hdfs://linfu1:9000"), conf, "root");
    }

    /**
     * 往hdfs上传文件
     *
     * @throws Exception
     */
    @Test
    public void testAddFileToHdfs() throws Exception {
        // 要上传的文件所在的本地路径
        Path src = new Path("C:\\Users\\admin\\Desktop\\oracle2.txt");
        // 要上传到hdfs的目标路径
        Path dst = new Path("/shilinfu");
        fs.copyFromLocalFile(src, dst);
        fs.close();
    }

    /**
     * 从hdfs中复制文件到本地文件系统
     *
     * @throws IOException
     * @throws IllegalArgumentException
     */
    @Test
    public void testDownloadFileToLocal() throws IllegalArgumentException, IOException {
        fs.copyToLocalFile(new Path("/shilinfu/test.txt"), new Path("C:\\Users\\admin\\Desktop"));
        fs.close();
    }

    @Test
    public void testMkdirAndDeleteAndRename() throws IllegalArgumentException, IOException {
        // 创建目录
        fs.mkdirs(new Path("/data/mining"));
        fs.mkdirs(new Path("/data/mining2"));
        // 删除文件夹 ，如果是非空文件夹，参数2必须给值true
        fs.delete(new Path("/data/mining"), true);
        // 重命名文件或文件夹
        fs.rename(new Path("/data/mining2"), new Path("/data/mining3"));
    }

    /**
     * 查看目录信息，只显示文件
     *
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws FileNotFoundException
     */
    @Test
    public void testListFiles() throws FileNotFoundException, IllegalArgumentException, IOException {
        // 思考：为什么返回迭代器，而不是List之类的容器
        RemoteIterator<LocatedFileStatus> listFiles = fs.listFiles(new Path("/"), true);
        while (listFiles.hasNext()) {
            LocatedFileStatus fileStatus = listFiles.next();
            System.out.println(fileStatus.getPath().getName());
            System.out.println(fileStatus.getBlockSize());
            System.out.println(fileStatus.getPermission());
            System.out.println(fileStatus.getLen());
            BlockLocation[] blockLocations = fileStatus.getBlockLocations();
            for (BlockLocation bl : blockLocations) {
                System.out.println("block-length:" + bl.getLength() + "--" + "block-offset:" + bl.getOffset());
                String[] hosts = bl.getHosts();
                for (String host : hosts) {
                    System.out.println(host);
                }
            }
            System.out.println("--------------为angelababy打印的分割线--------------");
        }
    }

    /**
     * 查看文件及文件夹信息
     *
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws FileNotFoundException
     */
    @Test
    public void testListAll() throws FileNotFoundException, IllegalArgumentException, IOException {
        FileStatus[] listStatus = fs.listStatus(new Path("/"));
        String flag = "d--             ";
        for (FileStatus fstatus : listStatus) {
            if (fstatus.isFile()) flag = "f--         ";
            System.out.println(flag + fstatus.getPath().getName());
        }
    }
}