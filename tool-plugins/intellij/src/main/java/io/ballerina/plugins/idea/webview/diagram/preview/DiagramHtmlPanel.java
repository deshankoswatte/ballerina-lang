package io.ballerina.plugins.idea.webview.diagram.preview;

import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

/**
 * A disposable HTML panel definition for diagram panel implementation.
 */
public interface DiagramHtmlPanel extends Disposable {

    @NotNull
    JComponent getComponent();

    void setHtml(@NotNull String html);

    void render();

    void setCSS(@Nullable String inlineCss, @NotNull String... fileUris);

}
