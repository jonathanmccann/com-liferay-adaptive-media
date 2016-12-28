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

package com.liferay.adaptive.media.image.processor.test;

import com.liferay.adaptive.media.AdaptiveMedia;
import com.liferay.adaptive.media.image.configuration.ImageAdaptiveMediaConfigurationEntry;
import com.liferay.adaptive.media.image.configuration.ImageAdaptiveMediaConfigurationHelper;
import com.liferay.adaptive.media.image.finder.ImageAdaptiveMediaFinder;
import com.liferay.adaptive.media.image.processor.ImageAdaptiveMediaProcessor;
import com.liferay.adaptive.media.image.test.util.DestinationReplacer;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.service.test.ServiceTestUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.registry.Registry;
import com.liferay.registry.RegistryUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Adolfo Pérez
 */
@RunWith(Arquillian.class)
@Sync
public class ImageAdaptiveMediaProcessorTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			SynchronousDestinationTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_configurationHelper = _getService(
			ImageAdaptiveMediaConfigurationHelper.class);
		_dlAppLocalService = _getService(DLAppLocalService.class);
		_finder = _getService(ImageAdaptiveMediaFinder.class);
		_processor = _getService(ImageAdaptiveMediaProcessor.class);

		ServiceTestUtil.setUser(TestPropsValues.getUser());

		Collection<ImageAdaptiveMediaConfigurationEntry> configurationEntries =
			_configurationHelper.getImageAdaptiveMediaConfigurationEntries(
				TestPropsValues.getCompanyId());

		for (ImageAdaptiveMediaConfigurationEntry configurationEntry :
				configurationEntries) {

			_configurationHelper.deleteImageAdaptiveMediaConfigurationEntry(
				TestPropsValues.getCompanyId(), configurationEntry.getUUID());
		}

		_addTestVariant();
	}

	@Ignore
	@Test
	public void testAddingFileEntryWithImageCreatesMedia() throws Exception {
		try (DestinationReplacer destinationReplacer = new DestinationReplacer(
				"liferay/adaptive_media_processor")) {

			ServiceContext serviceContext =
				ServiceContextTestUtil.getServiceContext(
					_group, TestPropsValues.getUserId());

			final FileEntry fileEntry = _addImageFileEntry(serviceContext);

			Stream<AdaptiveMedia<ImageAdaptiveMediaProcessor>> stream =
				_finder.getAdaptiveMedia(
					queryBuilder ->
						queryBuilder.allForFileEntry(fileEntry).done());

			Assert.assertEquals(_getVariantsCount(), stream.count());
		}
	}

	@Test
	public void testAddingFileEntryWithNoImageCreatesNoMedia()
		throws Exception {

		try (DestinationReplacer destinationReplacer = new DestinationReplacer(
				"liferay/adaptive_media_processor")) {

			ServiceContext serviceContext =
				ServiceContextTestUtil.getServiceContext(
					_group, TestPropsValues.getUserId());

			FileEntry fileEntry = _addNonImageFileEntry(serviceContext);

			Stream<AdaptiveMedia<ImageAdaptiveMediaProcessor>> stream =
				_finder.getAdaptiveMedia(
					queryBuilder ->
						queryBuilder.allForFileEntry(fileEntry).done());

			Assert.assertEquals(0, stream.count());
		}
	}

	@Test
	public void testCleaningFileEntryWithImageRemovesMedia() throws Exception {
		try (DestinationReplacer destinationReplacer = new DestinationReplacer(
				"liferay/adaptive_media_processor")) {

			ServiceContext serviceContext =
				ServiceContextTestUtil.getServiceContext(
					_group, TestPropsValues.getUserId());

			final FileEntry fileEntry = _addImageFileEntry(serviceContext);

			_processor.cleanUp(fileEntry.getLatestFileVersion());

			Stream<AdaptiveMedia<ImageAdaptiveMediaProcessor>> stream =
				_finder.getAdaptiveMedia(
					queryBuilder ->
						queryBuilder.allForFileEntry(fileEntry).done());

			Assert.assertEquals(0, stream.count());
		}
	}

	@Test
	public void testCleaningFileEntryWithNoImageDoesNothing() throws Exception {
		try (DestinationReplacer destinationReplacer = new DestinationReplacer(
				"liferay/adaptive_media_processor")) {

			ServiceContext serviceContext =
				ServiceContextTestUtil.getServiceContext(
					_group, TestPropsValues.getUserId());

			final FileEntry fileEntry = _addNonImageFileEntry(serviceContext);

			_processor.cleanUp(fileEntry.getLatestFileVersion());

			Stream<AdaptiveMedia<ImageAdaptiveMediaProcessor>> stream =
				_finder.getAdaptiveMedia(
					queryBuilder ->
						queryBuilder.allForFileEntry(fileEntry).done());

			Assert.assertEquals(0, stream.count());
		}
	}

	private FileEntry _addImageFileEntry(ServiceContext serviceContext)
		throws Exception {

		return _dlAppLocalService.addFileEntry(
			TestPropsValues.getUserId(), _group.getGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			StringUtil.randomString(), ContentTypes.IMAGE_JPEG,
			_getImageBytes(), serviceContext);
	}

	private FileEntry _addNonImageFileEntry(ServiceContext serviceContext)
		throws Exception {

		return _dlAppLocalService.addFileEntry(
			TestPropsValues.getUserId(), _group.getGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			StringUtil.randomString(), ContentTypes.APPLICATION_OCTET_STREAM,
			_getNonImageBytes(), serviceContext);
	}

	private void _addTestVariant() throws Exception {
		Map<String, String> properties = new HashMap<>();

		properties.put("height", "100");
		properties.put("width", "100");

		_configurationHelper.addImageAdaptiveMediaConfigurationEntry(
			TestPropsValues.getCompanyId(), "small", "0", properties);
	}

	private byte[] _getImageBytes() throws Exception {
		return FileUtil.getBytes(
			ImageAdaptiveMediaProcessorTest.class,
			"/com/liferay/adaptive/media/image/processor/test/dependencies" +
				"/image.jpg");
	}

	private byte[] _getNonImageBytes() {
		String s = StringUtil.randomString();

		return s.getBytes();
	}

	private <T> T _getService(Class<T> clazz) {
		try {
			Registry registry = RegistryUtil.getRegistry();

			return registry.getService(clazz);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private int _getVariantsCount() throws Exception {
		Collection<ImageAdaptiveMediaConfigurationEntry> configurationEntries =
			_configurationHelper.getImageAdaptiveMediaConfigurationEntries(
				TestPropsValues.getCompanyId());

		return configurationEntries.size();
	}

	private ImageAdaptiveMediaConfigurationHelper _configurationHelper;
	private DLAppLocalService _dlAppLocalService;
	private ImageAdaptiveMediaFinder _finder;

	@DeleteAfterTestRun
	private Group _group;

	private ImageAdaptiveMediaProcessor _processor;

}