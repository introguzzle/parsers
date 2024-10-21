package ru.introguzzle.parser.xml;

import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parser.xml.token.DeclarationToken;
import ru.introguzzle.parser.xml.token.ElementHeadToken;
import ru.introguzzle.parser.xml.token.Token;

import java.util.List;

public class XMLParser extends Parser {
    @Override
    public @Nullable XMLDocument parse(@Nullable String data) {
        List<Token> tokens = getTokenizer().tokenize(data);

        Token token = tokens.getFirst();
        assert token instanceof DeclarationToken;
        DeclarationToken declarationToken = (DeclarationToken) token;

        Token root = tokens.stream()
                .filter(ElementHeadToken.class::isInstance)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No root element found"));

        return new XMLDocument(
                declarationToken.getVersion(),
                declarationToken.getEncoding(),
                ((ElementHeadToken) root).toXMLElement()
        );
    }
}
