<p>
<f:message key="firstuse.message.welcome"/>
</p>
<p>
<f:message key="firstuse.newpassword"/> <%= Encryption.getEncryptedProperty(Environment.PROP_BASE_ADMIN_PASSWORD) %>
</p>
<p>
<f:message key="firstuse.message.warning"/>
</p>
<%-- FIXME: hard coding --%>
<a href="<jamwiki:link value="Special:Admin" />">Admin Page</a>