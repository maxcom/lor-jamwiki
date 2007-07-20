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
<c:if test="${!badinput}">
	<c:if test="${empty diffs}"><div class="message"><f:message key="diff.nochange" /></div></c:if>
	<c:if test="${!empty diffs}">
<table class="diff">
		<c:set var="previousLineNumber" value="-10" />
		<c:forEach items="${diffs}" var="diff">
			<c:if test="${diff.lineNumber > (previousLineNumber + 1)}">
<tr><td colspan="4" class="diff-line"><f:message key="diff.line" /> <c:out value="${diff.lineNumber}" />:</td></tr>
			</c:if>
<tr>
			<c:if test="${!empty diff.oldLine && diff.change}">
	<td class="diff-indicator">-</td>
	<td class="diff-delete"><c:out value="${diff.oldLine}" /></td>
			</c:if>
			<c:if test="${empty diff.oldLine || !diff.change}">
	<td class="diff-no-indicator">&#160;</td>
	<td class="diff-unchanged"><c:out value="${diff.oldLine}" /></td>
			</c:if>
			<c:if test="${!empty diff.newLine && diff.change}">
	<td class="diff-indicator">+</td>
	<td class="diff-add"><c:out value="${diff.newLine}" /></td>
			</c:if>
			<c:if test="${empty diff.newLine || !diff.change}">
	<td class="diff-no-indicator">&#160;</td>
	<td class="diff-unchanged"><c:out value="${diff.newLine}" /></td>
			</c:if>
</tr>
			<c:set var="previousLineNumber" value="${diff.lineNumber}" />
		</c:forEach>
</table>
	</c:if>
</c:if>
<c:if test="${badinput=='true'}">
	<f:message key="diff.badinput" />
</c:if>
