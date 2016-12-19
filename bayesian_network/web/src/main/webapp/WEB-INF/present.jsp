<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" isELIgnored="false" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>

<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<form action="bayesianServlet">
		<c:forEach items="${requestScope.attributeNameList}" var="currAttributeName">
			<c:out value="${currAttributeName}?"></c:out>
			<select name="${currAttributeName}">
			<c:forEach items="${requestScope.attributeValuesNameMap[currAttributeName]}" var="currAttributeName2">
				<c:out value="${currAttributeName2}"></c:out>
  				<option value=${currAttributeName2}>${currAttributeName2}</option>
			</c:forEach>
			</select>
			<br>
		</c:forEach>
		<input type="submit">
</form>
		
</body>
</html>