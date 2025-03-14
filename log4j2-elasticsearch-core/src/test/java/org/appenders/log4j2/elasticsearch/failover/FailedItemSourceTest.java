package org.appenders.log4j2.elasticsearch.failover;

/*-
 * #%L
 * log4j2-elasticsearch
 * %%
 * Copyright (C) 2019 Rafal Foltynski
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.appenders.log4j2.elasticsearch.ItemSource;
import org.appenders.log4j2.elasticsearch.StringItemSource;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FailedItemSourceTest {

    @Test
    public void getSourceDelegatesToItemSource() {

        // given
        FailedItemInfo failedItemInfo = mock(FailedItemInfo.class);
        StringItemSource itemSource = mock(StringItemSource.class);
        FailedItemSource<String> failedItemSource = new FailedItemSource<>(itemSource, failedItemInfo);

        String expectedSource = UUID.randomUUID().toString();
        when(itemSource.getSource()).thenReturn(expectedSource);

        // when
        String result = failedItemSource.getSource();

        // then
        assertEquals(expectedSource, result);

    }

    @Test
    public void releaseDelegatesToItemSource() {

        // given
        FailedItemInfo failedItemInfo = mock(FailedItemInfo.class);
        ItemSource<Object> itemSource = mock(ItemSource.class);
        FailedItemSource<Object> failedItemSource = new FailedItemSource<>(itemSource, failedItemInfo);

        // when
        failedItemSource.release();

        // then
        verify(itemSource, times(1)).release();

    }

    @Test
    public void failureIdCanBeChanged() {

        // given
        ItemSource<Object> itemSource = mock(ItemSource.class);
        FailedItemSource failedItemSource = new FailedItemSource<>(
                itemSource,
                mock(FailedItemInfo.class));
        CharSequence expectedFailureId = UUID.randomUUID().toString();

        assertNull(failedItemSource.getFailureId());

        // when
        failedItemSource.setFailureId(expectedFailureId);

        // then
        assertEquals(expectedFailureId, failedItemSource.getFailureId());

    }

    @Test
    public void targetNameCanBeRetrieved() {

        // given
        String expectedTargetName = UUID.randomUUID().toString();
        FailedItemInfo failedItemInfo = new FailedItemInfo(expectedTargetName);

        // when
        FailedItemSource failedItemSource = new FailedItemSource<Object>(
                mock(ItemSource.class),
                failedItemInfo);

        // then
        assertEquals(expectedTargetName, failedItemSource.getInfo().getTargetName());
    }

}
