package org.genedb.web.mvc.controller.download;

import org.genedb.web.mvc.model.BerkeleyMapFactory;
import org.genedb.web.mvc.model.TranscriptDTO;

import org.apache.log4j.Logger;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

public class DtoDataFetcher implements DataFetcher<Integer> {

    private Logger logger = Logger.getLogger(DtoDataFetcher.class);

    private BerkeleyMapFactory bmf;

    public TroubleTrackingIterator<String> iterator(List<Integer> ids, String expression, String fieldDelim) {
        return new DtoDataRowIterator(ids, bmf, expression, fieldDelim);
    }

    public void setBmf(BerkeleyMapFactory bmf) {
        this.bmf = bmf;
    }
}


class DtoDataRowIterator implements TroubleTrackingIterator<String> {

    private static final ExpressionParser parser = new SpelExpressionParser();

    private Logger logger = Logger.getLogger(DtoDataRowIterator.class);

    private BerkeleyMapFactory bmf;

    private Iterator<Integer> it;

    private String fieldDelim;

    private TranscriptDTO nextDTO;

    private Expression expression;

    private List<Integer> problems = Lists.newArrayList();

    public DtoDataRowIterator(List<Integer> ids, BerkeleyMapFactory bmf, String expressionString, String fieldDelim) {
        this.it = ids.iterator();
        this.bmf = bmf;
        this.fieldDelim = fieldDelim;
        this.expression = createExpression(expressionString);
    }

    @Override
    public boolean hasNext() {
        while (it.hasNext()) {
            Integer next = it.next();
            nextDTO = bmf.getDtoMap().get(next);
            if (nextDTO != null) {
                return true;
            } else {
                problems.add(next);
            }
        }
        return false;
    }

    @Override
    public String next() {
        //Integer id = it.next();
        // Need to convert name to featureId
        return getDescription(nextDTO, expression);
    }

    public TranscriptDTO peek() {
    	return nextDTO;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    public List<Integer> getProblems() {
        return problems;
    }

    public Expression createExpression(String expression) {
        return parser.parseExpression(expression, new TemplatedParserContext());
    }

//    public String getDescription(TranscriptDTO dto, String expression) {
//        Expression exp = createExpression(expression);
//        return getDescription(dto, exp);
//    }

    public String getDescription(TranscriptDTO dto, Expression expression) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("row", dto);

        TranscriptDTOAdaptor dta = new TranscriptDTOAdaptor(dto, fieldDelim);
        context.setRootObject(dta);

        return expression.getValue(context, String.class);
    }

}

