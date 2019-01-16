package ch.pschatzmann.stocks.strategy.optimization.annealing;

import java.io.Serializable;

public interface State extends Cloneable, Serializable { 
    void step();
    void undo();
    double result();
    Object clone();
}