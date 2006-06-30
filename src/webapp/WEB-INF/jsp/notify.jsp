<%
String action = request.getParameter("action");
String topic = request.getParameter("topic");
if (topic == null || topic.equals("")) throw new Exception("Topic must be specified");
String user = "";
Cookie[] cookies = request.getCookies();
if (cookies != null && cookies.length > 0) {
	for (int i = 0; i < cookies.length; i++) {
		if (cookies[i].getName().equals("username")) {
			user = cookies[i].getValue();
		}
	}
}
if (user == null || user.equals("")) {
	throw new Exception("User name not found.");
}
Notify notifier = new FileNotify(virtualWiki, topic);
if (action == null || action.equals("Notify Me")) {
	notifier.addMember(user);
} else {
	notifier.removeMember(user);
}
String next = "Wiki?" + topic;
%>

<jsp:forward page='<%=next%>'/>
