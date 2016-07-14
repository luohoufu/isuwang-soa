package com.isuwang.dapeng.bootstrap;

import com.isuwang.dapeng.bootstrap.classloader.AppClassLoader;
import com.isuwang.dapeng.bootstrap.classloader.ClassLoaderManager;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.URL;
import java.util.List;

/**
 * Created by tangliu on 2016/7/14.
 */
public class DynamicThreadHandler implements Runnable {

    private Socket socket;

    public DynamicThreadHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        DataInputStream dis = null;
        FileOutputStream fos = null;
        DataOutputStream dos = null;

        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            String fileName = dis.readUTF();
            long fileLength = dis.readLong();

            File dynamic = new File(Bootstrap.enginePath, "dynamic");
            if (!dynamic.exists() || !dynamic.isDirectory()) {
                dynamic.mkdirs();
            }

            File[] files = dynamic.listFiles();
            for (File file : files) {
                if (file.getName().equals(fileName)) {
                    dos.writeUTF("FILE_EXIST");
                    return;
                }
            }
            dos.writeUTF("READY");

            fos = new FileOutputStream(new File(dynamic, fileName));

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
            System.out.println("接收文件(" + fileName + ")完成");

            dos.writeUTF("200 OK");
            dos.flush();

            fos.close();

            /**
             * 加载文件
             */
            final File appsPath = new File(dynamic, fileName);
            Bootstrap.loadAppsUrls(appsPath);

            List<URL> appURL = Bootstrap.appURLs.get(Bootstrap.appURLs.size() - 1);
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
            Method registryServiceService = zookeeperRegistryClass.getMethod("registryService", Object.class);
            registryServiceService.invoke(zookeeperRegistryClass, context);

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
            try {

                if (dis != null)
                    dis.close();

                if (dos != null)
                    dos.close();

                if (fos != null)
                    fos.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("socket关闭啦");
        }

    }
}
