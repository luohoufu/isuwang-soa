package com.isuwang.soa.doc;

import com.isuwang.soa.doc.cache.ServiceCache;
import com.isuwang.soa.remoting.fake.json.JSONPost;
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
    private ServiceCache serviceCache;

    private String host = "127.0.0.1";
    /**
     * 远程端口
     */
    private int port = 9090;

    private JSONPost jsonPost = new JSONPost(host, port, true);

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public String test(HttpServletRequest req) {

        String jsonParameter = req.getParameter("parameter");
        String serviceName = req.getParameter("serviceName");
        String versionName = req.getParameter("version");
        String methodName = req.getParameter("methodName");

        com.isuwang.soa.core.metadata.Service service = serviceCache.getService(serviceName, versionName);

        try {
            return jsonPost.callServiceMethod(serviceName, versionName, methodName, jsonParameter, service);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
