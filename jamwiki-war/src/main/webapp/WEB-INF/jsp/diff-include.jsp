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
<c:choose>
	<c:when test="${!badinput}">
		<c:choose>
			<c:when test="${empty diffs}"><div class="message"><fmt:message key="diff.nochange" /></div></c:when>
			<c:otherwise>
				<div id="diff">
					<c:set var="previousPosition" value="-10" />
					<c:forEach items="${diffs}" var="diff">
						<c:if test="${diff.position > (previousPosition + 1)}">
							<div class="diff-line"><fmt:message key="diff.line" /> <c:out value="${diff.position + 1}" />:</div>
						</c:if>
						<div class="diff-entry">
							<c:choose>
								<c:when test="${!empty diff.oldText && diff.change}">
									<div class="diff-indicator">-</div>
									<div class="diff-delete">
										<c:set var="subDiffChange" value="false" />
										<c:forEach items="${diff.subDiffs}" var="subDiff"><c:if test="${!subDiffChange && subDiff.change}"><c:set var="subDiffChange" value="true" /><span class="diff-change"></c:if><c:if test="${subDiffChange && !subDiff.change}"><c:set var="subDiffChange" value="false" /></span></c:if><c:out value="${subDiff.oldText}" /></c:forEach>
										<c:if test="${subDiffChange}"></span></c:if>
										&#160;
									</div>
								</c:when>
								<c:otherwise>
									<div class="diff-indicator">&#160;</div>
									<div class="diff-unchanged"><c:out value="${diff.oldText}" />&#160;</div>
								</c:otherwise>
							</c:choose>
							<c:choose>
								<c:when test="${!empty diff.newText && diff.change}">
									<div class="diff-indicator">+</div>
									<div class="diff-add">
										<c:set var="subDiffChange" value="false" />
										<c:forEach items="${diff.subDiffs}" var="subDiff"><c:if test="${!subDiffChange && subDiff.change}"><c:set var="subDiffChange" value="true" /><span class="diff-change"></c:if><c:if test="${subDiffChange && !subDiff.change}"><c:set var="subDiffChange" value="false" /></span></c:if><c:out value="${subDiff.newText}" /></c:forEach>
										<c:if test="${subDiffChange}"></span></c:if>
										&#160;
									</div>
								</c:when>
								<c:otherwise>
									<div class="diff-indicator">&#160;</div>
									<div class="diff-unchanged"><c:out value="${diff.newText}" />&#160;</div>
								</c:otherwise>
							</c:choose>
							<div class="clear"></div>
						</div>
						<c:set var="previousPosition" value="${diff.position}" />
					</c:forEach>
				</div>
			</c:otherwise>
		</c:choose>
	</c:when>
	<c:otherwise><fmt:message key="diff.badinput" /></c:otherwise>
</c:choose>
