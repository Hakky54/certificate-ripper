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
package nl.altindag.crip.util;

import picocli.CommandLine;
import picocli.CommandLine.IHelpFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class HelpFactory implements IHelpFactory {

    private static final String OPTION_SEPARATOR = ",";
    private static final String EMPTY = "";

    @Override
    public CommandLine.Help create(CommandLine.Model.CommandSpec commandSpec, CommandLine.Help.ColorScheme colorScheme) {
        return new CommandLine.Help(commandSpec, colorScheme) {
            IParamLabelRenderer paramLabelRenderer;

            @Override
            public IParamLabelRenderer parameterLabelRenderer() {
                return paramLabelRenderer == null ? super.parameterLabelRenderer() : paramLabelRenderer;
            }

            @Override
            protected Ansi.Text createDetailedSynopsisOptionsText(Collection<CommandLine.Model.ArgSpec> done, List<CommandLine.Model.OptionSpec> optionList, Comparator<CommandLine.Model.OptionSpec> optionSort, boolean clusterBooleanOptions) {
                paramLabelRenderer = new IParamLabelRenderer() {
                    @Override
                    public Ansi.Text renderParameterLabel(CommandLine.Model.ArgSpec argSpec, Ansi ansi, List<Ansi.IStyle> styles) {
                        return ansi.text(EMPTY);
                    }

                    @Override
                    public String separator() {
                        return null;
                    }
                };
                Ansi.Text result = super.createDetailedSynopsisOptionsText(done, optionList, optionSort, clusterBooleanOptions);
                paramLabelRenderer = null;
                return result;
            }

            @Override
            public IOptionRenderer createDefaultOptionRenderer() {
                return (option, ignored, scheme) -> makeOptionList(option, scheme);
            }
        };
    }

    private static CommandLine.Help.Ansi.Text[][] makeOptionList(CommandLine.Model.OptionSpec option, CommandLine.Help.ColorScheme scheme) {
        String longOption = option.longestName();
        String shortOption = longOption.equals(option.shortestName()) ? EMPTY : option.shortestName();

        if (option.negatable()) {
            CommandLine.INegatableOptionTransformer transformer = option.command().negatableOptionTransformer();
            shortOption = transformer.makeSynopsis(shortOption, option.command());
            longOption = transformer.makeSynopsis(longOption, option.command());
        }

        String[] description = option.description();
        CommandLine.Help.Ansi.Text[] descriptionFirstLines = scheme.text(description[0]).splitLines();

        CommandLine.Help.Ansi.Text empty = CommandLine.Help.Ansi.OFF.text(EMPTY);
        List<CommandLine.Help.Ansi.Text[]> result = new ArrayList<>();
        result.add(new CommandLine.Help.Ansi.Text[]{
                scheme.optionText(String.valueOf(option.command().usageMessage().requiredOptionMarker())),
                scheme.optionText(shortOption),
                scheme.text(EMPTY.equals(shortOption) ? EMPTY : OPTION_SEPARATOR),
                scheme.optionText(longOption),
                descriptionFirstLines[0]}
        );

        for (int i = 1; i < descriptionFirstLines.length; i++) {
            result.add(new CommandLine.Help.Ansi.Text[]{empty, empty, empty, empty, descriptionFirstLines[i]});
        }

        if (option.command().usageMessage().showDefaultValues()) {
            CommandLine.Help.Ansi.Text defaultValue = scheme.text("  Default: " + option.defaultValueString(true));
            result.add(new CommandLine.Help.Ansi.Text[]{empty, empty, empty, empty, defaultValue});
        }

        return result.toArray(new CommandLine.Help.Ansi.Text[result.size()][]);
    }

}
