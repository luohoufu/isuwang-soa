package com.isuwang.dapeng.bootstrap.dynamic;

import com.isuwang.dapeng.bootstrap.Bootstrap;
import com.isuwang.dapeng.bootstrap.classloader.AppClassLoader;
import com.isuwang.dapeng.bootstrap.classloader.ClassLoaderManager;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by tangliu on 2016/7/14.
 */
public class DynamicThreadHandler implements Runnable {

    private Socket socket;

    private AtomicInteger clientId;

    private static ConcurrentHashMap<AtomicInteger,List<AppClassLoader>> tmpAppClassLoaders = new ConcurrentHashMap<>();

    public DynamicThreadHandler(Socket socket, AtomicInteger clientId) {
        this.socket = socket;
        this.clientId = clientId;
    }

    @Override
    public void run() {

        FileOutputStream fos = null;

        try (
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            String serviceName = dis.readUTF();
            String fileName = dis.readUTF();
            long fileLength = dis.readLong();

            // TODO: 2016/7/15 判断容器中已存在服务
            for (Object key : Bootstrap.dynamicServicesInfo.keySet()) {
                for (DynamicInfo info : Bootstrap.dynamicServicesInfo.get(key)){
                    if (info.getServiceName().equals(serviceName)) {
                        dos.writeUTF("SERVICE_EXIST");
                        return;
                    }
                }
            }

            dos.writeUTF("READY");

            File tmp = new File(Bootstrap.enginePath, "tmp");
            if (!tmp.exists() || !tmp.isDirectory()) {
                tmp.mkdirs();
            }
            File tmpZipFile = new File(tmp, fileName);
            fos = new FileOutputStream(tmpZipFile);

            byte[] sendBytes = new byte[1024];
            int transLen = 0;

            System.out.println("开始接受服务文件:" + fileName + ", 大小为:" + fileLength);
            while (true) {
                int read = 0;
                read = dis.read(sendBytes);
                if (read == -1)
                    break;
                transLen += read;
                fos.write(sendBytes, 0, read);
                fos.flush();

                if (transLen >= fileLength)
                    break;
            }
            System.out.println("接收服务文件(" + fileName + ")完成");

            dos.writeUTF("200 OK");
            dos.flush();

            fos.close();

            System.out.println(">>开始解压缩到dynamic文件夹<<");
            File dynamic = new File(Bootstrap.enginePath, "dynamic");
            if (!dynamic.exists() || !dynamic.isDirectory()) {
                dynamic.mkdirs();
            }

            try {
                decompress(tmpZipFile, dynamic);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(">>解压缩完成<<");
            tmpZipFile.delete();


            File[] serviceFiles = dynamic.listFiles();

            tmpAppClassLoaders.put(clientId,new ArrayList<>());

            Bootstrap.dynamicServicesInfo.put(clientId,new ArrayList<>());

            for (File file : serviceFiles) {
                if (serviceRunning(file))
                    continue;
                else
                    loadService(file, serviceName, clientId);

            }

            while (true) {
                try {
                    String msg = dis.readUTF();
                    System.out.println(msg);
                } catch (Exception e) {
                    System.out.println("socket is not connected");
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("socket关闭啦");
            Bootstrap.dynamicServicesInfo.remove(clientId);
            deleteDynamicService(clientId);
            for(AppClassLoader appClassLoader : tmpAppClassLoaders.get(clientId)) {
                // 移除容器临时的AppClassLoader
                ClassLoaderManager.appClassLoaders.remove(appClassLoader);
            }
            // 删除对应clientId的AppClassLoader
            tmpAppClassLoaders.remove(clientId);
        }

    }

    private static boolean serviceRunning(File file) {

        for(Object key : Bootstrap.dynamicServicesInfo.keySet()) {
            for(DynamicInfo info : Bootstrap.dynamicServicesInfo.get(key)) {
                File f = info.getServiceFile();
                if (f.getName().equals(file.getName()))
                    return true;
            }
        }

        return false;
    }

    private static void loadService(File appPath, String serviceName, AtomicInteger clientId) {

        try {
            System.out.println(">>开始加载服务<<");
            /**
             * 加载文件
             */
//            final File appPath = new File(dynamic, fileName);
            List<URL> appURL = Bootstrap.loadAppsUrl(appPath);
            AppClassLoader appClassLoader = new AppClassLoader(appURL.toArray(new URL[appURL.size()]));
            ClassLoaderManager.appClassLoaders.add(appClassLoader);

            /**
             * 加载服务
             */
            Class<?> springContainerClass = ClassLoaderManager.platformClassLoader.loadClass("com.isuwang.dapeng.container.spring.SpringContainer");
            Method loadDynamicServiceMethod = springContainerClass.getMethod("loadDynamicService", ClassLoader.class);
            Object context = loadDynamicServiceMethod.invoke(springContainerClass, appClassLoader);

            /**
             * 注册服务
             */
            Class<?> zookeeperRegistryClass = ClassLoaderManager.platformClassLoader.loadClass("com.isuwang.dapeng.container.registry.ZookeeperRegistryContainer");
            Method registryServiceService = zookeeperRegistryClass.getMethod("registryService", Object.class, Boolean.class, AtomicInteger.class);
            registryServiceService.invoke(zookeeperRegistryClass, context, true ,clientId);

            tmpAppClassLoaders.get(clientId).add(appClassLoader);

            DynamicInfo info = new DynamicInfo();
            info.setServiceName(serviceName);
            info.setServiceFile(appPath);
//            info.setAppUrl(appURL);
//            info.setAppClassLoader(appClassLoader);
//            info.setContext(context);

            Bootstrap.dynamicServicesInfo.get(clientId).add(info);
            System.out.println(">>加载服务完成<<");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从zookeeper移除对应Socket Client的服务
     */
    private static void deleteDynamicService(AtomicInteger clientId) {
        try {
            Class<?> zookeeperRegistryClass = ClassLoaderManager.platformClassLoader.loadClass("com.isuwang.dapeng.container.registry.ZookeeperRegistryContainer");
            Method getTmpServiceMethod = zookeeperRegistryClass.getMethod("getTmpService",AtomicInteger.class);
            List<String> services = (List<String>) getTmpServiceMethod.invoke(zookeeperRegistryClass,clientId);

            // 移除ProcessorCache里面的临时服务
            Method deleteFromProcessorCacheMethod = zookeeperRegistryClass.getMethod("deleteFromProcessorCache",AtomicInteger.class);
            deleteFromProcessorCacheMethod.invoke(zookeeperRegistryClass, clientId);

            //从zookeeper删除临时服务
            Class<?> zookeeperHelperClass = ClassLoaderManager.platformClassLoader.loadClass("com.isuwang.dapeng.registry.zookeeper.ZookeeperHelper");

            //Method deleteServiceInfoService = zookeeperHelperClass.getMethod("deleteService", String.class, String.class);
            Method deleteServiceInfoService = zookeeperHelperClass.getDeclaredMethod("deleteService", String.class, String.class);


            for (String info : services) {
                String[] infos = info.split(":");
                String serviceName = infos[0];
                String version = infos[1];

                deleteServiceInfoService.invoke(zookeeperHelperClass, serviceName, version);
            }
            services.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private static final int BUFFER = 1024;

    /**
     * 解压缩
     *
     * @param srcFile  源文件
     * @param destFile 目标文件夹
     * @throws Exception
     */
    public static void decompress(File srcFile, File destFile) throws Exception {

        CheckedInputStream cis = new CheckedInputStream(new FileInputStream(srcFile), new CRC32());

        ZipInputStream zis = new ZipInputStream(cis);

        decompress(destFile, zis);

        zis.close();
    }

    /**
     * 文件 解压缩
     *
     * @param destFile 目标文件
     * @param zis      ZipInputStream
     * @throws Exception
     */
    private static void decompress(File destFile, ZipInputStream zis)
            throws Exception {

        ZipEntry entry = null;
        while ((entry = zis.getNextEntry()) != null) {

            // 文件
            String dir = destFile.getPath() + File.separator + entry.getName();

            File dirFile = new File(dir);

            // 文件检查
            fileProber(dirFile);

            if (entry.isDirectory()) {
                dirFile.mkdirs();
            } else {
                decompressFile(dirFile, zis);
            }

            zis.closeEntry();
        }
    }

    /**
     * 文件探针
     * <p/>
     * <p/>
     * 当父目录不存在时，创建目录！
     *
     * @param dirFile
     */
    private static void fileProber(File dirFile) {

        File parentFile = dirFile.getParentFile();
        if (!parentFile.exists()) {

            // 递归寻找上级目录
            fileProber(parentFile);

            parentFile.mkdir();
        }

    }

    /**
     * 文件解压缩
     *
     * @param destFile 目标文件
     * @param zis      ZipInputStream
     * @throws Exception
     */
    private static void decompressFile(File destFile, ZipInputStream zis)
            throws Exception {

        BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(destFile));

        int count;
        byte data[] = new byte[BUFFER];
        while ((count = zis.read(data, 0, BUFFER)) != -1) {
            bos.write(data, 0, count);
        }

        bos.close();
    }
}
