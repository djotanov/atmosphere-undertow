package org.atmosphere.samples.chat;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.servlet.util.ImmediateInstanceFactory;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import org.atmosphere.config.AtmosphereAnnotation;
import org.atmosphere.config.service.*;
import org.atmosphere.cpr.AnnotationScanningServletContainerInitializer;
import org.atmosphere.cpr.AtmosphereInitializer;
import org.atmosphere.cpr.AtmosphereServlet;
import org.xnio.ByteBufferSlicePool;
import org.xnio.OptionMap;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Bootstrap {
    public static void main(String[] args) throws Exception {
        // deploy to undertow
        DeploymentInfo servletBuilder = Servlets.deployment()
                .setClassLoader(Bootstrap.class.getClassLoader())
                .setContextPath("")
                .setDeploymentName("chat")
                .setDefaultEncoding("UTF-8")
                .setUrlEncoding("UTF-8")
                .setResourceManager(new FileResourceManager(new File(""), 0))
                .addWelcomePage("index.html");
        servletBuilder.addServlet(Servlets.servlet("AtmosphereServlet", AtmosphereServlet.class)
                .addInitParam("org.atmosphere.cpr.AtmosphereHandler", "org.atmosphere.samples.chat.Chat")
                .addMapping("/chat/*")
                .setAsyncSupported(true));

        final WebSocketDeploymentInfo webSocketDeploymentInfo = new WebSocketDeploymentInfo();
        servletBuilder.addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, webSocketDeploymentInfo);

        DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
        manager.deploy();

        HttpHandler servletHandler = manager.start();
        PathHandler path = Handlers.path(Handlers.redirect("/")).addPrefixPath("/", servletHandler);
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "0.0.0.0")
                .setHandler(path)
                .build();
        server.start();
    }
}
