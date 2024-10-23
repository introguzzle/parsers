package ru.introguzzle.parser.xml;

import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parser.xml.token.DeclarationToken;
import ru.introguzzle.parser.xml.token.ElementHeadToken;
import ru.introguzzle.parser.xml.token.Token;

import java.io.Serial;
import java.util.List;

public class XMLParser extends Parser {
    @Serial
    private static final long serialVersionUID = 7848273065459808396L;

    @Override
    public @Nullable XMLDocument parse(@Nullable String data) throws XMLParseException {
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
