package ar.edu.itba.ss.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Either<A, B> {

  private final A value;
  private final B alternative;

  private Either(final A value, final B alternative) {
    this.value = value;
    this.alternative = alternative;
  }

  /**
   * Instantiates an Either object with the primary value, leaving the alternative in null
   *
   * @param value the primary value
   * @param <A> the primary value type
   * @param <B> the alternative value type
   * @return the Either instance with the given primary value
   */
  public static <A, B> Either<A, B> value(final A value) {
    if (value == null) {
      throw new IllegalArgumentException("The value cannot be null.");
    }
    return new Either<>(value, null);
  }

  /**
   * Instantiates an Either object with the right value, leaving the left in null
   *
   * @param alternative the alternative value
   * @param <A> the primary value type
   * @param <B> the alternative value type
   * @return the Either instance with the given alternative value
   */
  public static <A, B> Either<A, B> alternative(final B alternative) {
    if (alternative == null) {
      throw new IllegalArgumentException("The alternative value cannot be null.");
    }
    return new Either<>(null, alternative);
  }

  /**
   * value getter
   *
   * @return the value
   */
  public A getValue() {
    if (!isValuePresent()) {
      throw new IllegalStateException("The value is not present.");
    }
    return value;
  }

  /**
   * alternative value getter
   *
   * @return the alternative value
   */
  public B getAlternative() {
    if (isValuePresent()) {
      throw new IllegalStateException("The alternative value is not present.");
    }
    return alternative;
  }

  /**
   * Checks if the value is present
   *
   * @return true if the value is present, false if this is an alternative
   */
  public boolean isValuePresent() {
    return value != null;
  }

  /**
   * Either value mapper
   *
   * @param valueMapper the value mapper
   * @param <R> the new value type
   * @return the new either
   */
  public <R> Either<R, B> map(final Function<A, R> valueMapper) {
    if (isValuePresent()) {
      return Either.value(valueMapper.apply(value));
    }
    return Either.alternative(alternative);
  }

  /**
   * Either value flat mapper
   *
   * @param mapper the mapper
   * @param <R> the new value type
   * @return the new either
   */
  public <R> Either<R, B> flatMap(final Function<A, Either<R, B>> mapper) {
    if (isValuePresent()) {
      return mapper.apply(value);
    }
    return Either.alternative(alternative);
  }

  /**
   * Cast alternative value to another type, useful when propagating errors
   *
   * @param <R> the new value type
   * @return the new either
   * @throws IllegalStateException if called when a value is present
   */
  public <R> Either<R, B> castAlternative() {
    if (isValuePresent()) {
      throw new IllegalStateException("Can't map alternative of Either when not an alternative value");
    }
    return Either.alternative(alternative);
  }

  /**
   * Convert to Optional
   *
   * @return the Optional of value
   */
  public Optional<A> toOptional() {
    return Optional.ofNullable(value);
  }

  /**
   * If a value is present, performs the given action on the value, otherwise does nothing.
   *
   * @param action the action to be performed, if a value is present
   * @throws NullPointerException if value is present and the given action is
   *         {@code null}
   */
  public void ifPresent(final Consumer<A> action) {
    if (isValuePresent()) {
      action.accept(value);
    }
  }

  /**
   * If a value is not present, performs the given action on the alternative, otherwise does nothing.
   *
   * @param action the action to be performed, if a value is not present
   * @throws NullPointerException if value is not present and the given action is
   *         {@code null}
   */
  public void ifNotPresent(final Consumer<B> action) {
    if (!isValuePresent()) {
      action.accept(alternative);
    }
  }

  /**
   * If a value is not present, performs the given action, otherwise does nothing.
   *
   * @param action the action to be performed, if a value is not present
   * @throws NullPointerException if value is not present and the given action is
   *         {@code null}
   */
  public void ifNotPresent(final Runnable action) {
    if (!isValuePresent()) {
      action.run();
    }
  }

  /**
   * Filters this either.
   *
   * If the value is not present, returns the same either (with alternative).
   * If the value is present, the given predicate is evaluated for the value:
   *  - If the value matches the predicate, returns the same either (with value).
   *  - Otherwise, returns a new either with the given alternative value.
   *
   * @param filter the filter to be applied
   * @param alternativeSupplier the alternative to be used if the predicate is false
   * @return the same either if the value is not present or it doesn't match
   *  the predicate, or a new either created with the given alternative supplier
   */
  public Either<A, B> filter(final Predicate<? super A> filter, final Supplier<B> alternativeSupplier) {
    return !isValuePresent() || filter.test(value) ? this : Either.alternative(alternativeSupplier.get());
  }

  /**
   * If the value is present, returns it.
   * Otherwise, throws an exception mapped from the alternative value
   *
   * @param exceptionMapper the alternative to exception mapper
   * @param <X> the exception type
   * @return the value if it is present
   * @throws X an exception of type X mapped from the alternative
   */
  public <X extends Throwable> A orElseThrow(final Function<B, X> exceptionMapper) throws X {
    if (isValuePresent()) {
      return value;
    }
    throw exceptionMapper.apply(alternative);
  }
}
