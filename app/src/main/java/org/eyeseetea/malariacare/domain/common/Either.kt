package org.eyeseetea.malariacare.domain.common

sealed class Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()

    val isRight get() = this is Right<R>
    val isLeft get() = this is Left<L>

    fun <L> left(a: L) = Left(a)
    fun <R> right(b: R) = Right(b)
}

fun <L, R> Either<L, R>.getOrThrow(): R {
    return when (this) {
        is Either.Left -> throw Exception("Return Either value is not possible because is left")
        is Either.Right -> value
    }
}

fun <L, R, T> Either<L, R>.fold(left: (L) -> T, right: (R) -> T): T =
    when (this) {
        is Either.Left -> left(value)
        is Either.Right -> right(value)
    }

fun <L, R, T> Either<L, R>.flatMap(f: (R) -> Either<L, T>): Either<L, T> =
    fold({ this as Either.Left }, f)

fun <L, R, T> Either<L, R>.map(f: (R) -> T): Either<L, T> =
    flatMap { Either.Right(f(it)) }