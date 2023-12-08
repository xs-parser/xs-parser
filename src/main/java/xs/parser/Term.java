package xs.parser;

/**
 * Term is used to refer to any of the three kinds of components which can appear in particles.
 * All ·Terms· are themselves ·Annotated Components·.
 * A basic term is an Element Declaration or a Wildcard.
 * A basic particle is a Particle whose {term} is a ·basic term·.
 */
public interface Term extends AnnotatedComponent {

}
