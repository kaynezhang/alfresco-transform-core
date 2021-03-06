/*
 * #%L
 * Alfresco Transform Core
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.transformer;

import static java.text.MessageFormat.format;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.alfresco.transformer.EngineClient.sendTRequest;
import static org.alfresco.transformer.TestFileInfo.testFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.springframework.http.HttpStatus.OK;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import com.google.common.collect.ImmutableSet;

/**
 * @author Cezar Leahu
 */
@RunWith(Parameterized.class)
public class AlfrescoPdfRendererTransformationIT
{
    private static final Logger logger = LoggerFactory.getLogger(
        AlfrescoPdfRendererTransformationIT.class);
    private static final String ENGINE_URL = "http://localhost:8090";

    private static final Map<String, TestFileInfo> TEST_FILES = Stream.of(
        testFile("application/pdf","pdf","quick.pdf"),
        testFile("application/illustrator","ai","quickCS3.ai")  ,      
        testFile("application/illustrator","ai","quickCS5.ai")
    ).collect(toMap(TestFileInfo::getPath, identity()));

    private final String sourceFile;
    private final String sourceMimetype;

    public AlfrescoPdfRendererTransformationIT(String sourceFile)
    {
        this.sourceFile = sourceFile;
        this.sourceMimetype = TEST_FILES.get(sourceFile).getMimeType();
    }

    @Parameterized.Parameters
    public static Set<String> engineTransformations()
    {
        return ImmutableSet.of(
            "quick.pdf",
            "quickCS3.ai",
            "quickCS5.ai"
        );
    }

    @Test
    public void testTransformation()
    {
        final String descriptor = format("Transform ({0}, {1} -> {2}, {3})",
            sourceFile, sourceMimetype, "image/png", "png");

        try
        {
            final ResponseEntity<Resource> response = sendTRequest(ENGINE_URL, sourceFile, sourceMimetype,
                "image/png", "png");
            assertEquals(descriptor, OK, response.getStatusCode());
        }
        catch (Exception e)
        {
            fail(descriptor + " exception: " + e.getMessage());
        }
    }
}
