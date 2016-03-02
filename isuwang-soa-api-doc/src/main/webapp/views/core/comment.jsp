<%@ page import="java.net.URLEncoder" %>
<%--<%@ page import="com.isuwang.api.doc.helpers.CookieHelper" %>--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    String[] colors = new String[]{"bs-callout-danger", "bs-callout-info", "bs-callout-warning"};
    int index = 0;
%>
<h3> 评论 </h3>
<hr/>
<div class="bs-docs-section">
    <c:forEach var="comment" items="${comments}" varStatus="vs">
        <div class="bs-callout <%=colors[index ++ % colors.length]%>">
            <c:choose>
                <c:when test="${comment.parentId > 0}">
                    <h4>${comment.userName}
                        <small>回复</small>
                            ${comment.replyUserName}</h4>
                </c:when>
                <c:otherwise>
                    <h4>${comment.userName}
                        <small>发表：</small>
                    </h4>
                </c:otherwise>
            </c:choose>

            <p data-marked-id="marked">${comment.content}</p>
            <a href="#replay"
               onclick="jumpToReply(${comment.id}, ${comment.userId}, '${comment.userName}')">回复</a><span
                style="float: right">${comment.createTimeStr}</span>
        </div>
    </c:forEach>
</div>
<!--评论框-->
<p id="replay" style="display: none"><span>回复  </span><span class="replyed_user_name"></span></p>

<%--<div class="Input_Box">--%>
    <%--<textarea id="Input_text" class="Input_text"></textarea>--%>

    <%--<div class="Input_Foot"><a class="postBtn"><% if (null != CookieHelper.getCookie(request, "CustomerUserName")) {%>--%>
        <%--发表评论<% } else { %>登陆<%}%></a>--%>
    <%--</div>--%>
<%--</div>--%>
<br/><br/>

<form id="comment_form" action="${basePath}/user/comment.htm" method="post" style="display: none">
    <input type="text" id="content" name="content" value=""/>
    <input type="text" id="reply_user_id" name="reply_user_id" value="0">
    <input type="text" id="reply_user_name" name="reply_user_name" value="">
    <input type="text" id="parent_id" name="parent_id" value="0">
    <input type="text" id="redirectURL" name="redirectURL" value="${redirectURL}"/>
</form>

<script>
    $("a.postBtn").click(function () {

        var data = {}
        data.content = $("textarea.Input_text").val()
        data.reply_user_id = $("input#reply_user_id").val()
        data.reply_user_name = $("input#reply_user_name").val()
        data.parent_id = $("input#parent_id").val()
        data.redirectURL = $("input#redirectURL").val()

        var fn = function (data) {
            if (data.status == 401) {
                <%
                   String redirectURL = request.getAttribute("redirectURL").toString();
                   redirectURL = URLEncoder.encode(redirectURL, "UTF-8");
                %>
                top.location.href = '${basePath}' + '/user/loginIndex.htm?redirectURL=' + '<%=redirectURL%>';
            } else {
                var result = $.parseJSON(data);
                if (result.code == 0) {
                    top.location.reload();
                }
            }
        }
        $.ajax({
            url: "${basePath}/comment/comment.htm",
            type: "post",
            data: data,
            success: fn,
            error: fn,
            dataType: 'json'
        });

        return false
    });

    function jumpToReply(commentId, userId, userName) {

        $("input#reply_user_id").val(userId)
        $("input#reply_user_name").val(userName)
        $("input#parent_id").val(commentId)

        $("span.replyed_user_name").html(userName)
        $("p#replay").css("display", "block")
    }
</script>