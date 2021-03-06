/*
 * #%L
 * Alfresco Transform Core
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.transformer.executors;

import org.alfresco.transform.exceptions.TransformException;
import org.alfresco.transformer.PdfRendererOptionsBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.alfresco.transformer.util.RequestParamMap.ALLOW_PDF_ENLARGEMENT;
import static org.alfresco.transformer.util.RequestParamMap.HEIGHT_REQUEST_PARAM;
import static org.alfresco.transformer.util.RequestParamMap.MAINTAIN_PDF_ASPECT_RATIO;
import static org.alfresco.transformer.util.RequestParamMap.PAGE_REQUEST_PARAM;
import static org.alfresco.transformer.util.RequestParamMap.TIMEOUT;
import static org.alfresco.transformer.util.RequestParamMap.WIDTH_REQUEST_PARAM;
import static org.alfresco.transformer.util.Util.stringToLong;

/**
 * CommandExecutor implementation for running PDF Renderer transformations. It runs the
 * transformation logic as a separate Shell process.
 */
public class PdfRendererCommandExecutor extends AbstractCommandExecutor
{
    private static String ID = "pdfrenderer";

    public static final String LICENCE = "This transformer uses alfresco-pdf-renderer which uses the PDFium library from Google Inc. See the license at https://pdfium.googlesource.com/pdfium/+/master/LICENSE or in /pdfium.txt";

    private final String EXE;

    public PdfRendererCommandExecutor(String exe)
    {
        if (exe == null || exe.isEmpty())
        {
            throw new IllegalArgumentException("PdfRendererCommandExecutor EXE variable cannot be null or empty");
        }
        this.EXE = exe;
        super.transformCommand = createTransformCommand();
        super.checkCommand = createCheckCommand();
    }

    @Override
    public String getTransformerId()
    {
        return ID;
    }

    @Override
    protected RuntimeExec createTransformCommand()
    {
        RuntimeExec runtimeExec = new RuntimeExec();
        Map<String, String[]> commandsAndArguments = new HashMap<>();
        commandsAndArguments.put(".*",
            new String[]{EXE, "SPLIT:${options}", "${source}", "${target}"});
        runtimeExec.setCommandsAndArguments(commandsAndArguments);

        Map<String, String> defaultProperties = new HashMap<>();
        defaultProperties.put("key", null);
        runtimeExec.setDefaultProperties(defaultProperties);

        runtimeExec.setErrorCodes("1");

        return runtimeExec;
    }

    @Override
    protected RuntimeExec createCheckCommand()
    {
        RuntimeExec runtimeExec = new RuntimeExec();
        Map<String, String[]> commandsAndArguments = new HashMap<>();
        commandsAndArguments.put(".*", new String[]{EXE, "--version"});
        runtimeExec.setCommandsAndArguments(commandsAndArguments);
        return runtimeExec;
    }

    @Override
    public void transform(String transformName, String sourceMimetype, String targetMimetype,
                          Map<String, String> transformOptions,
                          File sourceFile, File targetFile) throws TransformException
    {
        final String options = PdfRendererOptionsBuilder
                .builder()
                .withPage(transformOptions.get(PAGE_REQUEST_PARAM))
                .withWidth(transformOptions.get(WIDTH_REQUEST_PARAM))
                .withHeight(transformOptions.get(HEIGHT_REQUEST_PARAM))
                .withAllowPdfEnlargement(transformOptions.get(ALLOW_PDF_ENLARGEMENT))
                .withMaintainPdfAspectRatio(transformOptions.get(MAINTAIN_PDF_ASPECT_RATIO))
                .build();

        Long timeout = stringToLong(transformOptions.get(TIMEOUT));

        run(options, sourceFile, targetFile, timeout);
    }
}
