/*
 * Copyright 2021 Thunderberry.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
module nl.altindag.certificate.ripper {

    requires transitive nl.altindag.ssl;
    requires transitive java.net.http;
    requires transitive info.picocli;
    requires transitive org.apache.commons.net;
    requires transitive org.simplejavamail;
    requires transitive org.simplejavamail.core;
    requires transitive nl.altindag.sude;
    requires transitive org.slf4j;

    exports nl.altindag.crip;
    exports nl.altindag.crip.request;
    exports nl.altindag.crip.model;

    opens nl.altindag.crip.command to info.picocli;
    opens nl.altindag.crip.command.print to info.picocli;
    opens nl.altindag.crip.command.export to info.picocli;

}