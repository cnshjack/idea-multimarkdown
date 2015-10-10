/*
 * Copyright (c) 2015-2015 Vladimir Schneider <vladimir.schneider@gmail.com>
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.vladsch.idea.multimarkdown.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FileReferenceLinkGitHubRules extends FileReferenceLink {
    protected String originalPrefix;

    public FileReferenceLinkGitHubRules(@NotNull String sourcePath, @NotNull String targetPath, Project project) {
        super(sourcePath, targetPath, project);
    }
    public FileReferenceLinkGitHubRules(@NotNull FileReference sourceReference, @NotNull FileReference targetReference) {
        super(sourceReference, targetReference);
    }
    public FileReferenceLinkGitHubRules(@NotNull VirtualFile sourceFile, @NotNull FileReference targetReference) {
        super(sourceFile, targetReference);
    }
    public FileReferenceLinkGitHubRules(@NotNull FileReference sourceReference, @NotNull VirtualFile targetFile) {
        super(sourceReference, targetFile);
    }
    public FileReferenceLinkGitHubRules(@NotNull VirtualFile sourceFile, @NotNull VirtualFile targetFile, Project project) {
        super(sourceFile, targetFile, project);
    }
    public FileReferenceLinkGitHubRules(@NotNull PsiFile sourceFile, @NotNull PsiFile targetFile) {
        super(sourceFile, targetFile);
    }
    public FileReferenceLinkGitHubRules(@NotNull FileReference sourceReference, @NotNull PsiFile targetFile) {
        super(sourceReference, targetFile);
    }
    public FileReferenceLinkGitHubRules(@NotNull PsiFile sourceFile, @NotNull FileReference targetReference) {
        super(sourceFile, targetReference);
    }

    @NotNull
    @Override
    protected String getWikiPageRefPathPrefix() {
        // for github wikis this is always "", all wiki page refs are without subdirectories
        return "";
    }

    public static class InaccessibleGitHubWikiPageReasons extends InaccessibleWikiPageReasons {
        InaccessibleGitHubWikiPageReasons(int reasons, String wikiRef, FileReferenceLinkGitHubRules referenceLink) {
            super(reasons, wikiRef, referenceLink);
        }

        @Override
        public boolean wikiRefHasSlash() { return (reasons & REASON_WIKI_PAGEREF_HAS_SLASH) != 0; }

        @Override
        public boolean wikiRefHasFixableSlash() { return (reasons & REASON_WIKI_PAGEREF_HAS_FIXABLE_SLASH) != 0; }

        @Override
        public String wikiRefHasSlashFixed() { return wikiRef.replace("/", ""); }

        @Override
        public boolean wikiRefHasSubDir() { return (reasons & REASON_WIKI_PAGEREF_HAS_SUBDIR) != 0; }

        @Override
        public String wikiRefHasSubDirFixed() { return new FilePathInfo(wikiRef).getFileNameWithAnchor(); }
    }

    @NotNull
    @Override
    public InaccessibleWikiPageReasons inaccessibleWikiPageRefReasons(@Nullable String wikiPageRef) {
        int reasons = computeWikiPageRefReasonsFlags(wikiPageRef);

        if (wikiPageRef != null) {
            if (wikiPageRef.contains("/")) {
                // see if it would resolve to the target without it
                FilePathInfo wikiPageRefInfo = new FilePathInfo(wikiPageRef);

                if (equivalentWikiRef(false, false, getWikiPageRef(), wikiPageRefInfo.getFileNameWithAnchor())) {
                    reasons |= REASON_WIKI_PAGEREF_HAS_SUBDIR;
                } else if (equivalentWikiRef(false, false, getWikiPageRef(), wikiPageRef.replace("/", ""))) {
                    reasons |= REASON_WIKI_PAGEREF_HAS_FIXABLE_SLASH;
                } else  {
                    reasons |= REASON_WIKI_PAGEREF_HAS_SLASH;
                }
            }
        }
        return new InaccessibleGitHubWikiPageReasons(reasons, wikiPageRef, this);
    }

    @Override
    protected int computeWikiPageRefReasonsFlags(@Nullable String wikiPageRef) {
        // add our own
        return super.computeWikiPageRefReasonsFlags(wikiPageRef);
    }

    @Override
    public int compareTo(FilePathInfo o) {
        int itmp;
        return (itmp = getUpDirectoriesToWikiHome() - o.getUpDirectoriesToWikiHome()) != 0 ? itmp :  super.compareTo(o);
    }

    // TEST: needs testing
    @Override
    protected void computeLinkRefInfo(@NotNull String sourceReferencePath, @NotNull String targetReferencePath) {
        super.computeLinkRefInfo(sourceReferencePath, targetReferencePath);
        originalPrefix = pathPrefix;

        if (!sourceReference.isWikiPage() && this.isUnderWikiHome() && project != null) {
            // here we resolve using github relative rules
            FilePathInfo adjustedInfo = resolveLinkRef(pathPrefix);
            if (adjustedInfo != null) {
                super.computeLinkRefInfo(sourceReferencePath, adjustedInfo.getFilePath());
            }
        }
    }

    @Override
    public String toString() {
        return "FileReferenceLinkGitHubRules" +
                innerString() +
                ")";
    }
}
