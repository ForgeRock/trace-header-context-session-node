/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2018 ForgeRock AS.
 */

package org.forgerock.openam.auth.nodes;

import com.google.inject.assistedinject.Assisted;
import com.sun.identity.shared.debug.Debug;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;
import org.forgerock.openam.core.CoreWrapper;

import javax.inject.Inject;


/** 
 *
 * This node prints context, header, session, cookies, callbacks, locales, and client IP information to the log file
 *
 * @author keith.daly
 * @version 1.0.0
 *
 */
@Node.Metadata(outcomeProvider  = SingleOutcomeNode.OutcomeProvider.class,
               configClass      = TraceHeaderContextSessionNode.Config.class)
public class TraceHeaderContextSessionNode extends SingleOutcomeNode {


    private final Config config;
    private final CoreWrapper coreWrapper;
    private final static String DEBUG_FILE = "TraceHeaderContextSessionNode";
    private Debug debug = Debug.getInstance(DEBUG_FILE);

    private LogLevel logLevel;


    public interface Config {
        @Attribute(order = 100)
        default LogLevel logLevel() {
            return LogLevel.ERROR;
        }
    }

    @Inject
    public TraceHeaderContextSessionNode(@Assisted Config config, CoreWrapper coreWrapper) throws NodeProcessException {
        this.config = config;
        this.coreWrapper = coreWrapper;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        loadConfig();
        Action.ActionBuilder actionBuilder = goToNext();

        String sContext = context.toString();
        String sHeaders = context.request.headers.toString();
        String sCookies = context.request.cookies.toString();
        String sSharedState = context.sharedState.toString();
        String sTransientState = context.transientState.toString();
        String sLocales = context.request.locales.getLocales().toString();
        String sClientIp = context.request.clientIp;
        String sXForwardedFor = context.request.headers.get("X-Forwarded-For").toString();
        String sCallbacks = context.getAllCallbacks().toString();

        debugMesssage("*** CONTEXT ***\n" + sContext);
        debugMesssage("*** HEADERS ***\n" + sHeaders);
        debugMesssage("*** COOKIES ***\n" + sCookies);
        debugMesssage("*** SHARED_STATE ***\n" + sSharedState);
        debugMesssage("*** TRANSIENT STATE ***\n" + sTransientState);
        debugMesssage("*** LOCALES ***\n" + sLocales);
        debugMesssage("*** CLIENT_IP ***\n" + sClientIp);
        debugMesssage("*** X-FORWARDED-FOR ***\n" + sXForwardedFor);
        debugMesssage("*** CALLBACKS ***\n" + sCallbacks);

        return actionBuilder.build();
    }

    private void loadConfig() {
        logLevel = config.logLevel();
    }

    private void debugMesssage (String message) {
        switch (logLevel) {
            case ERROR:
                debug.error("[" + DEBUG_FILE + "] : " + message);
                break;
            case WARNING:
                debug.warning("[" + DEBUG_FILE + "] : " + message);
                break;
            case MESSAGE:
                debugMesssage("[" + DEBUG_FILE + "] : " + message);
                break;
        }
    }

    private void debugMesssage (String message, Throwable e) {
        switch (logLevel) {
            case ERROR:
                debug.error("[" + DEBUG_FILE + "] : " + message, e);
                break;
            case WARNING:
                debug.warning("[" + DEBUG_FILE + "] : " + message, e);
                break;
            case MESSAGE:
                debugMesssage("[" + DEBUG_FILE + "] : " + message, e);
                break;
        }
    }

    public enum LogLevel {
        ERROR,
        WARNING,
        MESSAGE
    }
}