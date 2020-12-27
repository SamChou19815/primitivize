const a = 876;

console.log(
  require('fs')
    .readFileSync('examples/critter-program.txt')
    .toString()
    .substring(a, a + 10)
);
