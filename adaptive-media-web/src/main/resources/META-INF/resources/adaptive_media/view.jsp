<%--
/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>

<%@ include file="/adaptive_media/init.jsp" %>

<aui:nav-bar cssClass="collapse-basic-search" markupView="lexicon">
	<aui:nav cssClass="navbar-nav">
		<portlet:renderURL var="viewImageConfigurationEntriesURL" />

		<aui:nav-item
			href="<%= viewImageConfigurationEntriesURL %>"
			label="image-resolutions"
			selected="<%= true %>"
		/>
	</aui:nav>
</aui:nav-bar>

<%
List<ImageAdaptiveMediaConfigurationEntry> configurationEntries = (List)request.getAttribute(AdaptiveMediaWebKeys.CONFIGURATION_ENTRIES_LIST);
%>

<liferay-frontend:management-bar
	disabled="<%= configurationEntries.size() <= 0 %>"
	includeCheckBox="<%= true %>"
	searchContainerId="imageConfigurationEntries"
>
	<liferay-frontend:management-bar-buttons>
		<liferay-frontend:management-bar-sidenav-toggler-button
			disabled="<%= true %>"
			icon="info-circle"
			label="info"
		/>

		<liferay-frontend:management-bar-display-buttons
			disabled="<%= true %>"
			displayViews='<%= new String[] {"list"} %>'
			portletURL="<%= currentURLObj %>"
			selectedDisplayStyle="list"
		/>
	</liferay-frontend:management-bar-buttons>

	<liferay-frontend:management-bar-filters>
		<liferay-frontend:management-bar-navigation
			disabled="<%= true %>"
			navigationKeys='<%= new String[] {"all"} %>'
			navigationParam="entriesNavigation"
			portletURL="<%= currentURLObj %>"
		/>
	</liferay-frontend:management-bar-filters>

	<liferay-frontend:management-bar-action-buttons>
		<liferay-frontend:management-bar-button href='<%= "javascript:" + renderResponse.getNamespace() + "deleteImageConfigurationEntries();" %>' icon="times" label="delete" />
	</liferay-frontend:management-bar-action-buttons>
</liferay-frontend:management-bar>

<%
PortletURL portletURL = renderResponse.createRenderURL();
%>

<div class="container-fluid-1280">
	<portlet:actionURL name="/adaptive_media/delete_image_configuration_entry" var="deleteImageConfigurationEntryURL" />

	<aui:form action="<%= deleteImageConfigurationEntryURL.toString() %>" method="post" name="fm">
		<liferay-ui:search-container
			emptyResultsMessage="there-are-no-image-resolutions"
			id="imageConfigurationEntries"
			iteratorURL="<%= portletURL %>"
			rowChecker="<%= new ImageConfigurationEntriesChecker(liferayPortletResponse) %>"
			total="<%= configurationEntries.size() %>"
		>
			<liferay-ui:search-container-results
				results="<%= configurationEntries %>"
			/>

			<liferay-ui:search-container-row
				className="com.liferay.adaptive.media.image.configuration.ImageAdaptiveMediaConfigurationEntry"
				modelVar="configurationEntry"
			>

				<%
				row.setPrimaryKey(String.valueOf(configurationEntry.getUUID()));
				%>

				<liferay-portlet:renderURL varImpl="rowURL">
					<portlet:param name="mvcRenderCommandName" value="/adaptive_media/edit_image_configuration_entry" />
					<portlet:param name="redirect" value="<%= currentURL %>" />
					<portlet:param name="entryUuid" value="<%= String.valueOf(configurationEntry.getUUID()) %>" />
				</liferay-portlet:renderURL>

				<liferay-ui:search-container-column-text
					cssClass="table-cell-content"
					href="<%= rowURL %>"
					name="name"
					orderable="<%= false %>"
					value="<%= configurationEntry.getName() %>"
				/>

				<liferay-ui:search-container-column-text
					name="uuid"
					orderable="<%= false %>"
					value="<%= configurationEntry.getUUID() %>"
				/>

				<%
				Map<String, String> properties = configurationEntry.getProperties();
				%>

				<liferay-ui:search-container-column-text
					name="max-width"
					orderable="<%= false %>"
					value='<%= properties.get("width") %>'
				/>

				<liferay-ui:search-container-column-text
					name="max-height"
					orderable="<%= false %>"
					value='<%= properties.get("height") %>'
				/>

				<liferay-ui:search-container-column-jsp
					path="/adaptive_media/image_configuration_entry_action.jsp"
				/>
			</liferay-ui:search-container-row>

			<liferay-ui:search-iterator displayStyle="list" markupView="lexicon" />
		</liferay-ui:search-container>
	</aui:form>
</div>

<aui:script>
	function <portlet:namespace />deleteImageConfigurationEntries() {
		if (confirm('<%= UnicodeLanguageUtil.get(request, "are-you-sure-you-want-to-delete-the-selected-entries") %>')) {
			var form = AUI.$(document.<portlet:namespace />fm);

			submitForm(form);
		}
	}
</aui:script>

<portlet:renderURL var="addImageConfigurationEntryURL">
	<portlet:param name="mvcRenderCommandName" value="/adaptive_media/edit_image_configuration_entry" />
	<portlet:param name="redirect" value="<%= currentURL %>" />
</portlet:renderURL>

<liferay-frontend:add-menu>
	<liferay-frontend:add-menu-item title='<%= LanguageUtil.get(request, "add-image-resolution") %>' url="<%= addImageConfigurationEntryURL %>" />
</liferay-frontend:add-menu>