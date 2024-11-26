package ru.introguzzle.parsers.xml.parse;

import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.util.Streams;
import ru.introguzzle.parsers.xml.entity.XMLDocument;

import java.io.Serial;
import java.util.List;

@ExtensionMethod(Streams.class)
class XMLParser extends Parser {
    @Serial
    private static final long serialVersionUID = 7848273065459808396L;

    @Override
    public @NotNull XMLDocument parse(@NotNull String data) throws XMLParseException {
        List<Token> tokens = getTokenizer().tokenize(data);

        Token first = tokens.getFirst();
        if (!(first instanceof DeclarationToken declarationToken)) {
            throw new XMLParseException("No declaration found");
        }

        Token root = tokens.stream()
                .select(ElementHeadToken.class)
                .findFirst()
                .orElseThrow(() -> new XMLParseException("No root element found"));

        return new XMLDocument(
                declarationToken.getVersion(),
                declarationToken.getEncoding(),
                ((ElementHeadToken) root).toXMLElement()
        );
    }
}
