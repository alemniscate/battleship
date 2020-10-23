package battleship;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Player 1, place your ships on the game field");
        System.out.println();

        BattleField bf1 = new BattleField(1, scanner);
        bf1.setting();
        System.out.println();       

        playerChange(scanner);

        System.out.println();       
        System.out.println("Player 2, place your ships on the game field");
        System.out.println();

        BattleField bf2 = new BattleField(2, scanner);
        bf2.setting();
        System.out.println();       

        while (true) {
            playerChange(scanner);
            bf2.shootBy(bf1);
            if (bf2.remainder == 0) {
                break;
            }

            playerChange(scanner);
            bf1.shootBy(bf2);
            if (bf1.remainder == 0) {
                break;
            }
        }

        System.out.println("You sank the last ship. You won. Congratulations!");

        scanner.close();
    }

    static void playerChange(Scanner scanner) {
        System.out.println("Press Enter and pass the move to another player");
        System.out.println();       
        scanner.nextLine();
        System.out.println();
    }
}

class BattleField {

    Scanner scanner;
    char[][] array;
    String errReason = "";
    int remainder = 0;
    final String SEPARATER = "---------------------";
    int playerNumber;

    BattleField(int playerNumber, Scanner scanner) {
        this.playerNumber = playerNumber;
        this.scanner = scanner;
        array = new char[10][10];
    }

    void setting() {
        Ship a = new Ship('A', 5, "Aircraft Carrier");
        Ship b = new Ship('B', 4, "Battleship");
        Ship c = new Ship('C', 3, "Cruiser");
        Ship d = new Ship('D', 2, "Destroyer");
        Ship s = new Ship('S', 3, "Submarine");
        display(false);

        setShip(a);
        display(false);

        setShip(b);
        display(false);

        setShip(s);
        display(false);

        setShip(c);
        display(false);

        setShip(d);
        display(false);
    }

    void shootBy(BattleField opponent) {
        display(true);
        System.out.println(SEPARATER);
        opponent.display(false);

        boolean result = false;
        while (!result) {
            System.out.println();
            System.out.println(String.format("Player %d, it's your turn:", opponent.playerNumber));
            System.out.println();
            String input = scanner.nextLine();
            System.out.println();

            Position pos = new Position(input);
            if (!checkCoordinate(pos)) {
                System.out.println("Error! You entered the wrong coordinates! Try again:");
                continue;
            }

            switch (array[pos.y][pos.x]) {
                case 0:
                    array[pos.y][pos.x] = 'M';
                    System.out.println("You missed. Try again:");
                    break;
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'S':
                    boolean sankedFlag = isSanked(pos);
                    array[pos.y][pos.x] = 'X';
                    remainder--;
                    if (remainder > 0) {
                        if (sankedFlag) {
                            System.out.println("You sank a ship! Specify a new target:");
                        } else {
                            System.out.println("You hit a ship! Try again:");
                        }
                    }
                    break;
                case 'X':
                    System.out.println("You hit a ship! Try again:");
                    break;
                case 'M':
                    System.out.println("You missed. Try again:");
                    break;
            }

            result = true;
        }
    }

    boolean isSanked(Position pos) {
        char shipId = array[pos.y][pos.x];
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                if (y == pos.y && x == pos.x) {
                    continue;
                }
                if (array[y][x] == shipId) {
                    return false;
                }
            }
        }
        return true;
    }

    void setShip(Ship ship) {
        System.out.println();
        System.out.println(String.format("Enter the coordinates of the %s (%d cells):", ship.name, ship.length));

        boolean result = false;
        while (!result) {
            System.out.println();
            String input = scanner.nextLine();
            System.out.println();

            String[] strs = input.split(" ");
            String start = strs[0];
            String end = strs[1];
            result = setPosition(ship, start, end);  
            if (!result) {
                switch (errReason) {
                    case "length":
                        System.out.println(String.format("Error! Wrong length of the %s! Try again:", ship.name));
                        break;
                    case "location":
                        System.out.println("Error! Wrong ship location! Try again:");
                        break;
                    case "too close":
                        System.out.println("Error! You placed it too close to another one. Try again:");
                        break;
                    case "coordinate":
                        System.out.println("Error! You entered the wrong coordinates! Try again:");
                        break;
                    }
            }
        }

        remainder += ship.length;
    }

    boolean setPosition(Ship ship, String start, String end) {
        Position startPos = new Position(start);
        Position endPos = new Position(end);
        if (!checkCoordinate(startPos) || !checkCoordinate(endPos)) {
            errReason = "coordinate";
            return false;
        }
        Position.swapIfNeed(startPos, endPos);

        int length = 0;
        if (startPos.x == endPos.x) {
            length = endPos.y - startPos.y + 1;
        } else if (startPos.y == endPos.y) {
            length = endPos.x - startPos.x + 1;
        } else {
            errReason = "location";
            return false;        
        }
        if (ship.length != length) {
            errReason = "length";
            return false;
        }

        if (startPos.x == endPos.x) {
            for (int y = startPos.y; y <= endPos.y; y++) {
                if (y >= 10) {
                    errReason = "location";
                    return false;               
                }
                if (array[y][startPos.x] != 0) {
                    errReason = "location";
                    return false;               
                }
            }
        }

        if (startPos.y == endPos.y) {
            for (int x = startPos.x; x <= endPos.x; x++) {
                if (x >= 10) {
                    errReason = "location";
                    return false;               
                }
                if (array[startPos.y][x] != 0) {
                    errReason = "location";
                    return false;               
                }
            }
        }

        List<Position> list = getNear(startPos, endPos);

        for (Position pos: list) {
            if (array[pos.y][pos.x] != 0) {
                errReason = "too close";
                return false;
            }
        }

        if (startPos.x == endPos.x) {
            for (int y = startPos.y; y <= endPos.y; y++) {
                array[y][startPos.x] = ship.id;
            }
        }

        if (startPos.y == endPos.y) {
            for (int x = startPos.x; x <= endPos.x; x++) {
                array[startPos.y][x] = ship.id;
            }
        }
        
        errReason = "";
        return true;
    }

    boolean checkCoordinate(Position pos) {
        if (pos.x > 9 || pos.x < 0 || pos.y > 9 || pos.y < 0) {
            return false;
        }
        return true;
    }

    List<Position> getNear(Position start, Position end) {
        List<Position> list = new ArrayList<Position>();

        if (start.x == end.x) {
            int x = start.x;
            for (int y = start.y; y <= end.y; y++) {
                if (x > 0) {
                    list.add(new Position(y, x - 1));
                }
                if (x < 9) {
                    list.add(new Position(y, x + 1));
                }
            }
            for (x = Math.max(start.x - 1, 0); x <= Math.min(start.x + 1, 9); x++) {
                if (start.y > 0) {
                    list.add(new Position(start.y - 1, x));
                }
                if (end.y < 9) {
                    list.add(new Position(end.y + 1, x));
                }
            }       
        }

        if (start.y == end.y) {
            int y = start.y;
            for (int x = start.x; x <= end.x; x++) {
                if (y > 0) {
                    list.add(new Position(y - 1, x));
                }
                if (y < 9) {
                    list.add(new Position(y + 1, x));
                }
            }
            for (y = Math.max(start.y - 1, 0); y <= Math.min(start.y + 1, 9); y++) {
                if (start.x > 0) {
                    list.add(new Position(y, start.x - 1));
                }
                if (end.x < 9) {
                    list.add(new Position(y, end.x + 1));
                }
            }       
        }

        return list;
    }

    void display(boolean hideShipFlag) {
        System.out.println("  1 2 3 4 5 6 7 8 9 10");
        for (int y = 0; y < 10; y++) {
            String line = Character.toString((char)('A' + y)) + " ";
            for (int x= 0; x < 10; x++) {
                switch (array[y][x]) {
                    case 0:
                        line +="~ ";
                        break;
                    case 'A':
                    case 'B':
                    case 'C':
                    case 'D':
                    case 'S':
                        if (hideShipFlag) {
                            line += "~ ";
                        } else {
                            line += "O ";
                        }
                        break;
                    case 'X':
                        line += "X ";
                        break;
                    case 'M':
                        line += "M ";
                        break;
                }
            }
            System.out.println(line);
        }
    }
}

class Position {
    int y;
    int x;

    Position (int y, int x) {
        this.y = y;
        this.x = x;
    }

    Position (String input) {
        if (input.length() == 0) {
            y = -1;
            x = -1;
            return;
        }
        y = input.charAt(0) - 'A';
        if (input.length() == 1) {
            x = -1;
            return;
        }
        x = Integer.parseInt(input.substring(1)) - 1;
    }

    @Override
    public boolean equals(Object other) {
        Position otherPos = (Position) other;
        if (x == otherPos.x && y == otherPos.y) {
            return true;
        }
        return false;
    }

    static void swapIfNeed(Position start, Position end) {
        if (start.x == end.x) {
            if (start.y > end.y) {
                int temp = start.y;
                start.y = end.y;
                end.y = temp;
            }
        }

        if (start.y == end.y) {
            if (start.x > end.x) {
                int temp = start.x;
                start.x = end.x;
                end.x = temp;
            }
        }
    }
}

class Ship {
    char id;
    int length;
    String name;

    Ship(char id, int length, String name) {
        this.id = id;
        this.length = length;
        this.name = name;
    }
}