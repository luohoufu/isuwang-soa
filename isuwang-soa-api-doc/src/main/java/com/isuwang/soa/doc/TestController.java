package com.isuwang.soa.doc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isuwang.soa.core.*;
import com.isuwang.soa.doc.codec.JSONSerializer;
import com.isuwang.soa.doc.restful.DataInfo;
import com.isuwang.soa.doc.restful.InvocationInfo;
import com.isuwang.soa.remoting.netty.SoaClient;
import com.isuwang.soa.remoting.netty.TSoaTransport;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
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
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    private static JSONSerializer jsonSerializer;

    private static String host = "127.0.0.1";
    /**
     * 远程端口
     */
    private static int port = 9090;

    public static void setHost(String host) {
        TestController.host = host;
    }

    public static void setPort(int port) {
        TestController.port = port;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public String test(HttpServletRequest req) {

        String jsonParameter = req.getParameter("parameter");
        String serviceName = req.getParameter("serviceName");
        String versionName = req.getParameter("version");
        String methodName = req.getParameter("methodName");

        try {
            return callServiceMethod(serviceName, versionName, methodName, jsonParameter);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public static String callServiceMethod(String serviceName, String versionName, String methodName, String jsonParameter) throws Exception {


        ObjectMapper objectMapper = new ObjectMapper();
        StringWriter out = new StringWriter();

        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> params = objectMapper.readValue(jsonParameter, Map.class);

        Map<String, Object> map = new HashMap<>();
        map.put("serviceName", serviceName);
        map.put("version", versionName);
        map.put("methodName", methodName);
        map.put("params", params);

        objectMapper.writeValue(out, map);

        //发起请求
        final InvocationInfo invocationInfo = new InvocationInfo();
        final DataInfo request = new DataInfo();
        request.setConsumesType("JSON");
        request.setConsumesValue(out.toString());
        request.setServiceName(serviceName);
        request.setVersion(versionName);
        request.setMethodName(methodName);
        invocationInfo.setDataInfo(request);

        final long beginTime = System.currentTimeMillis();

        LOGGER.info("soa-request: {}", out.toString());

        String jsonResponse = post(invocationInfo);

        LOGGER.info("soa-response: {} {}ms", jsonResponse, System.currentTimeMillis() - beginTime);

        return jsonResponse;
    }


    private static void initContext(DataInfo data) {
        InvocationContext context = InvocationContext.Factory.getCurrentInstance();

        context.setSeqid(1);

        SoaHeader soaHeader = new SoaHeader();
        try {
            soaHeader.setCallerIp(Optional.of(InetAddress.getLocalHost().getHostAddress()));
        } catch (UnknownHostException e) {
            LOGGER.error(e.getMessage(), e);
        }
        soaHeader.setServiceName(data.getServiceName());
        soaHeader.setMethodName(data.getMethodName());
        soaHeader.setVersionName(data.getVersion());
        soaHeader.setCallerFrom(Optional.of("web"));

        context.setHeader(soaHeader);
        context.setCalleeTimeout(45000);
    }

    private static String post(InvocationInfo invocationInfo) {

        String jsonResponse = "{}";

        SoaClient client = null;
        TSoaTransport inputSoaTransport = null;
        TSoaTransport outputSoaTransport = null;

        try {
            client = new SoaClient(host, port);

            initContext(invocationInfo.getDataInfo());
            InvocationContext context = InvocationContext.Factory.getCurrentInstance();
            SoaHeader soaHeader = context.getHeader();

            final ByteBuf requestBuf = Unpooled.buffer(8192);
            outputSoaTransport = new TSoaTransport(requestBuf);

            TSoaServiceProtocol outputProtocol;

            outputProtocol = new TSoaServiceProtocol(outputSoaTransport, true);
            outputProtocol.writeMessageBegin(new TMessage(invocationInfo.getDataInfo().getServiceName() + ":" + invocationInfo.getDataInfo().getMethodName(), TMessageType.CALL, context.getSeqid()));
            jsonSerializer.write(invocationInfo, outputProtocol);
            outputProtocol.writeMessageEnd();
            outputSoaTransport.flush();//在报文头部写入int,代表报文长度(不包括自己)

            /*
            if (client == null) {
                throw new SoaException(SoaBaseCode.NotConnected);
            }
            */
            ByteBuf responseBuf = client.send(context.getSeqid(), requestBuf); //发送请求，返回结果

            if (null != responseBuf) {

                inputSoaTransport = new TSoaTransport(responseBuf);
                TSoaServiceProtocol inputProtocol = new TSoaServiceProtocol(inputSoaTransport, true);

                TMessage msg = inputProtocol.readMessageBegin();
                if (TMessageType.EXCEPTION == msg.type) {
                    TApplicationException x = TApplicationException.read(inputProtocol);
                    inputProtocol.readMessageEnd();
                    throw x;
                } else if (context.getSeqid() != msg.seqid) {
                    throw new TApplicationException(4, soaHeader.getMethodName() + " failed: out of sequence response");
                } else {
                    if ("0000".equals(soaHeader.getRespCode().get())) {
                        // 接收返回包
                        jsonSerializer.read(invocationInfo, inputProtocol);
                        inputProtocol.readMessageEnd();
                    } else {
                        throw new SoaException(soaHeader.getRespCode().get(), soaHeader.getRespMessage().get());
                    }

                    return invocationInfo.getResponseData();
                }

            } else {
                throw new SoaException(SoaBaseCode.TimeOut);
            }

        } catch (SoaException e) {

            LOGGER.error(e.getMsg());
            jsonResponse = String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\"}", e.getCode(), e.getMsg(), "{}");

        } catch (TException e) {

            LOGGER.error(e.getMessage(), e);
            jsonResponse = String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\"}", "9999", e.getMessage(), "{}");

        } catch (Exception e) {

            LOGGER.error(e.getMessage(), e);
            jsonResponse = String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\"}", "9999", "系统繁忙，请稍后再试[9999]！", "{}");

        } finally {
            if (client != null) {
                client.close();
                client.shutdown();
            }

            if (outputSoaTransport != null)
                outputSoaTransport.close();

            if (inputSoaTransport != null)
                inputSoaTransport.close();

            InvocationContext.Factory.removeCurrentInstance();
        }

        return jsonResponse;
    }

}
