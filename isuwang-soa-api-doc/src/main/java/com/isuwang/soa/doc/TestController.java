package com.isuwang.soa.doc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isuwang.soa.core.SoaException;
import com.isuwang.soa.doc.codec.JSONSerializer;
import com.isuwang.soa.doc.restful.DataInfo;
import com.isuwang.soa.doc.restful.InvocationInfo;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TIOStreamTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试Controller
 *
 * @author tangliu
 * @date 15/10/8
 */
@Controller
@RequestMapping("test")
public class TestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestController.class);

    @ModelAttribute
    public void populateModel(Model model) {
        model.addAttribute("tagName", "test");
    }

    @Autowired
    private JSONSerializer jsonSerializer;


    private static String host = "127.0.0.1";
    /**
     * 远程端口
     */
    private static int port = 9091;
    /**
     * 超时时间
     */
    private static int timeout = 35000;// 35秒
    /**
     * 超时时间
     */
    private static int connectTimeout = 10000;// 10秒
    /**
     * 多路服务
     */
    private static boolean multiplexed = true;

    private static boolean verbose = true;


    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public String test(HttpServletRequest req) {

        String jsonParameter = req.getParameter("parameter");
        String serviceName = req.getParameter("serviceName");
        String version = req.getParameter("version");
        String methodName = req.getParameter("methodName");

        try {
            //生成json请求参数
            ObjectMapper objectMapper = new ObjectMapper();
            StringWriter out = new StringWriter();

            Map<String, Map<String, Object>> params = objectMapper.readValue(jsonParameter, Map.class);

            Map<String, Object> map = new HashMap<>();
            map.put("serviceName", serviceName);
            map.put("version", version);
            map.put("methodName", methodName);
            map.put("params", params);

            objectMapper.writeValue(out, map);

            //发起请求
            final InvocationInfo invocationInfo = new InvocationInfo();
            final DataInfo request = new DataInfo();
            request.setConsumesType("JSON");
            request.setConsumesValue(out.toString());
            invocationInfo.setDataInfo(request);
            invocationInfo.setMultiplexed(multiplexed);

            final long beginTime = System.currentTimeMillis();

            LOGGER.info("soa-request: {}", out.toString());

            String jsonResponse = post(invocationInfo);

            LOGGER.info("soa-response: {} {}ms", jsonResponse, System.currentTimeMillis() - beginTime);

            return jsonResponse;

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return null;
    }

    String post(InvocationInfo invocationInfo) {

        String jsonResponse = "{}";

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), connectTimeout);
            socket.setSoTimeout(timeout);
            socket.setKeepAlive(true);

            final InputStream input = new BufferedInputStream(socket.getInputStream());
            final OutputStream output = new BufferedOutputStream(socket.getOutputStream());
            final TIOStreamTransport transport = new TIOStreamTransport(input, output);
            final TCompactProtocol protocol = new TCompactProtocol(new TFramedTransport(transport));

            // 发送请求包
            jsonSerializer.write(invocationInfo, protocol);

            // 接收返回包
            jsonSerializer.read(invocationInfo, protocol);

            jsonResponse = invocationInfo.getResponseData();
        } catch (ConnectException e) {
            LOGGER.error(e.getMessage(), e);

            LOGGER.warn("连接接口失败 " + host + ":" + port);

            jsonResponse = String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\"}", "AA98", "系统繁忙，请稍后再试！[AA98]", "{}");
        } catch (SocketTimeoutException e) {
            LOGGER.error(e.getMessage(), e);

            LOGGER.warn("等待接口超时 " + host + ":" + port);

            jsonResponse = String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\"}", "AA96", "系统繁忙，请稍后再试！[AA96]", "{}");
        } catch (SoaException e) {
            jsonResponse = String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\"}", e.getCode(), e.getMsg(), "{}");
        } catch (TException e) {
            LOGGER.error(e.getMessage(), e);

            jsonResponse = String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\"}", "9999", e.getMessage(), "{}");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);

            LOGGER.warn("接口通讯异常" + host + ":" + port);

            jsonResponse = String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\"}", "9999", "系统繁忙，请稍后再试[9999]！", "{}");
        }

        return jsonResponse;
    }

}
