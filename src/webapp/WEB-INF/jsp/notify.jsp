<%--

  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.

  This program is free software; you can redistribute it and/or modify
  it under the terms of the latest version of the GNU Lesser General
  Public License as published by the Free Software Foundation;

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this program (LICENSE.txt); if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

--%>
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
