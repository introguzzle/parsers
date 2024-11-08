package ru.introguzzle.parsers.xml.parse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parsers.xml.entity.XMLDocument;
import ru.introguzzle.parsers.xml.token.DeclarationToken;
import ru.introguzzle.parsers.xml.token.ElementHeadToken;
import ru.introguzzle.parsers.xml.token.Token;

import java.io.Serial;
import java.util.List;

public class XMLParser extends Parser {
    @Serial
    private static final long serialVersionUID = 7848273065459808396L;

    @Override
    public @Nullable XMLDocument parse(@NotNull String data) throws XMLParseException {
        List<Token> tokens = getTokenizer().tokenize(data);

        Token token = tokens.getFirst();
        if (!(token instanceof DeclarationToken declarationToken)) {
            throw new XMLParseException("No declaration found");
        }

        Token root = tokens.stream()
                .filter(ElementHeadToken.class::isInstance)
                .findFirst()
                .orElseThrow(() -> new XMLParseException("No root element found"));

        return new XMLDocument(
                declarationToken.getVersion(),
                declarationToken.getEncoding(),
                ((ElementHeadToken) root).toXMLElement()
        );
    }
}
