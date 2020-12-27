var veryLongVar = 69;
var isGoodRecorder = 69;
var a = 1;
var b = 2;

fun reassign(d: int): void = veryLongVar = d
fun constant(): int = 4 * 3 + 2
fun foo(): void = veryLongVar = constant()
fun isGood(): bool = isGoodRecorder == 1

fun testRecursion(): int = 1 + testRecursion()

fun main(): void =
  if isGood() then ({
    reassign(3 + 2);
    isGoodRecorder = testRecursion()
  }) else if smell() < energy() + nearby(3 + 2) then ({
    foo();
    wait()
  }) else (
    forward()
  )
