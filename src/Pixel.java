import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import javalib.impworld.*;
import javalib.worldimages.*;

// Represents the History of a given ImageSimulation (stored in Grid)
class History {
  Deque<SeamInfo> hist;
  
  History() {
    this.hist = new ArrayDeque<>();
  }
  
  // Removes the first item from this History
  SeamInfo pop() {
    SeamInfo first = hist.removeFirst();
    return first;
  }
  
  // Adds the given SeamInfo to the front of this History
  void add(SeamInfo seam) {
    hist.addFirst(seam);
  }
  
  // Is this History empty?
  boolean isEmpty() {
    return this.hist.isEmpty();
  }
}

// Represents a Pixel that can be on a grid (Border or non-border)
interface IPixel {

  //determines if this and the given pixel are the same pixel
  boolean sameAs(IPixel that);
  
  //determines if this and the given border are the same
  boolean sameBorder(Border that);
  
  //determines if this and given pixel are the same.
  boolean samePixel(Pixel that);
  
  // determines the integrity of this IPixel
  // defined as all neighbors it has being properly linked to each other
  boolean integrious();

  // computes the brightness of this IPixel
  double computeBrightness();

  // determines the neighboring pixel based on the given direction
  IPixel determineNeighbor(String direction);


  // updates the neighbor pixel based on the given direction
  void updateNeighbor(String direction, IPixel neigh, boolean tellNeighbor);

  // overload for updateNeighbor if we know all four directions
  void updateNeighbor(IPixel up, IPixel down, IPixel left, IPixel right);

  // computes the total energy that this pixel produces
  double computeEnergy();

  // computes the total vertical energy that this pixel produces
  double computeVerticalEnergy();

  // computes the total horizontal energy that this pixel produces
  double computeHorizontalEnergy();

  // Determines if this IPixel is a border pixel
  boolean isBorder();

  // EFFECT: removes this IPixel from itself by relinking all of it's neighbors
  // taking into account what moves above (assuming nothing moves above, pass through null)
  void removeSelf(IPixel parent, boolean isVert);
  
  //determines what the given pixels relationship is to this pixel
  int determineParentIdx(IPixel that);
  
  //updates how this IPixel renders using the given color
  void updateRenderColor(Color color);
  
  // EFFECT: reinserts this IPixel by re-linking its neighbors 
  void reinsertSelf(boolean isVert);
  
  // EFFECT: resets the render color of this IPixel
  void resetRenderColor();
}

//A border represents the edges past an image, and have no purpose when
//rendering and computing seams.
class Border implements IPixel {
  Color color;
  Color renderColor;

  Border() {
    this.color = Color.black;
    this.renderColor = Color.black;
  }

  
  //determines if this border is the same as the given pixel
  public boolean sameAs(IPixel that) {
    return that.sameBorder(this);
  }
  
  //determines if this border is the same as the given border
  public boolean sameBorder(Border that) {
    //because all borders serve the same purpose, they are the same
    return true;
  }
  
  //determines if this border is the same as the given pixel
  public boolean samePixel(Pixel that) {
    //borders can never be pixels, so false is always returned
    return false;
  }
  
  // determines if this pixel has integrity
  public boolean integrious() {
    return true;
  }

  // computes the brightness of this Border
  public double computeBrightness() {
    return 0;
  }

  // determines the neighboring pixel based on the given direction
  public IPixel determineNeighbor(String direction) {
    throw new IllegalArgumentException("border has no neighbors");
  }


  // updates the neighbor pixel based on the given direction
  public void updateNeighbor(String direction, IPixel neigh, boolean tellNeighbor) {
    if (tellNeighbor) { 
      if (direction.equals("left")) {
        neigh.updateNeighbor("right", this, false);
      }
      if (direction.equals("right")) {
        neigh.updateNeighbor("left", this, false);
      }
      if (direction.equals("up")) {
        neigh.updateNeighbor("down", this, false);
      }
      if (direction.equals("down")) {
        neigh.updateNeighbor("up", this, false);
      }
      
    }
    // Borders have no Neighbors, so nothing to update
  }

  // overload for updateNeighbor if we know all four directions
  public void updateNeighbor(IPixel up, IPixel down, IPixel left, IPixel right) {
    // borders have no neighbors, so nothing to update
  }

  //removes this border based on the parent
  public void removeSelf(IPixel parent, boolean isVert) {
    //because this is a border, nothing needs to be removed.
  }
  
  // computes the total energy that this border produces
  public double computeEnergy() {
    return 0;
  }

  // computes the total vertical energy that this border produces
  public double computeVerticalEnergy() {
    return 0;
  }

  // computes the total horizontal energy that this border produces
  public double computeHorizontalEnergy() {
    return 0;
  }

  // Determines if this Border is a Border pixel
  public boolean isBorder() {
    return true;
  }
  
  //updates how this IPixel renders using the given color
  public void updateRenderColor(Color color) {
    this.renderColor = color;
  }
  
  //determines what direction the parents came from 
  public int determineParentIdx(IPixel that) {
    throw new IllegalArgumentException("Border has no parents");
  }
  
  // EFFECT: reinserts this Border by re-linking its neighbors 
  public void reinsertSelf(boolean isVert) {
    // Borders can never be reinserted as they can never be removed.
  }
  
  // EFFECT: resets the render color of this Border
  public void resetRenderColor() {
    // render color should never change for borders
  }
}

//A pixel represents a singular cell that holds a color and knows it's neighbors
//it has no context of where it is in the world
class Pixel implements IPixel {
  IPixel left;
  IPixel right;
  IPixel up;
  IPixel down;
  Color color;
  Color renderColor;

  Pixel(Color color) {
    this.color = color;
    this.renderColor = color;
  }
  
  //determines if the given IPixel is the same as this pixel
  public boolean sameAs(IPixel that) {
    return that.samePixel(this);
  }
  
  //determines if the given Pixel is the same as this pixel
  public boolean samePixel(Pixel that) {
    //because pixels are tied to more than their color / neighbors, we use .equals() here
    return this.equals(that);
  }
  
  //determines if the given border is the same as this pixel
  public boolean sameBorder(Border that) {
    return false;
  }
  
  // determines if this pixel has integrity
  public boolean integrious() {
    return this == this.left.determineNeighbor("right")
        && this == this.right.determineNeighbor("left") 
        && this == this.down.determineNeighbor("up")
        && this == this.up.determineNeighbor("down");
  }

  // computes the brightness of this pixel
  public double computeBrightness() {
    return (this.color.getRed() + this.color.getBlue() + this.color.getGreen()) / 255.0;
  }

  // determines the neighboring pixel based on the given direction
  public IPixel determineNeighbor(String direction) {
    if (direction.equals("up")) {
      return this.up;
    }
    if (direction.equals("down")) {
      return this.down;
    }
    if (direction.equals("left")) {
      return this.left;
    }
    if (direction.equals("right")) {
      return this.right;
    }

    if (direction.equals("upLeft") || direction.equals("leftUp")) {
      if (this.determineNeighbor("up").isBorder()) {
        return this.determineNeighbor("up");
      }
      return this.up.determineNeighbor("left");
    }
    if (direction.equals("upRight") || direction.equals("rightUp")) {
      if (this.determineNeighbor("up").isBorder()) {
        return this.determineNeighbor("up");
      }
      return this.up.determineNeighbor("right");
    }
    if (direction.equals("downLeft") || direction.equals("leftDown")) {
      if (this.determineNeighbor("down").isBorder()) {
        return this.determineNeighbor("down");
      }
      return this.down.determineNeighbor("left");
    }
    if (direction.equals("downRight") || direction.equals("rightDown")) {
      if (this.determineNeighbor("down").isBorder()) {
        return this.determineNeighbor("down");
      }
      return this.down.determineNeighbor("right");
    }
    throw new IllegalArgumentException("Invalid direction provided!");
  }



  // EFFECT: updates the neighbor pixel based on the given direction
  public void updateNeighbor(String direction, IPixel neigh, boolean tellNeighbor) {
    if (direction.equals("left")) {
      this.left = neigh;
      if (tellNeighbor) {
        neigh.updateNeighbor("right", this, false);
      }
    }
    else if (direction.equals("right")) {
      this.right = neigh;
      if (tellNeighbor) {
        neigh.updateNeighbor("left", this, false);
      }
    }
    else if (direction.equals("up")) {
      this.up = neigh;
      if (tellNeighbor) {
        neigh.updateNeighbor("down", this, false);
      }
    }
    else if (direction.equals("down")) {
      this.down = neigh;
      if (tellNeighbor) {
        neigh.updateNeighbor("up", this, false);
      }
    }
    else {
      throw new IllegalArgumentException("Invalid direction named " + direction);
    }
  }

  // overload for updateNeighbor if we know all four directions
  public void updateNeighbor(IPixel up, IPixel down, IPixel left, IPixel right) {
    this.updateNeighbor("left", left, true);
    this.updateNeighbor("right", right, true);
    this.updateNeighbor("up", up, true);
    this.updateNeighbor("down", down, true);
  }

  // computes the total energy that this pixel produces
  public double computeEnergy() {
    return Math.sqrt(Math.pow(computeHorizontalEnergy(), 2) + Math.pow(computeVerticalEnergy(), 2));
  }

  // computes the total vertical energy that this pixel produces
  public double computeVerticalEnergy() {
    double tBrightness = this.determineNeighbor("up").computeBrightness();
    double dBrightness = this.determineNeighbor("down").computeBrightness();
    double tlBrightness;
    double trBrightness;
    double dlBrightness;
    double drBrightness;
    if (determineNeighbor("down").isBorder()) {
      dlBrightness = 0;
      drBrightness = 0;
    }
    else {
      dlBrightness = determineNeighbor("downLeft").computeBrightness();
      drBrightness = determineNeighbor("downRight").computeBrightness();
    }
    if (determineNeighbor("up").isBorder()) {
      tlBrightness = 0;
      trBrightness = 0;
    }
    else {
      tlBrightness = determineNeighbor("upLeft").computeBrightness();
      trBrightness = determineNeighbor("upRight").computeBrightness();
    }
    return (tlBrightness + 2 * tBrightness + trBrightness)
        - (dlBrightness + 2 * dBrightness + drBrightness);
  }

  // computes the total horizontal energy that this pixel produces
  public double computeHorizontalEnergy() {
    // based on how diagonals are computed, we have to first make sure the
    // vertical components exist.
    // if they do not, then they are borders, and their brightness is the same
    // brightness as a border piece
    double tlBrightness;
    double trBrightness;
    if (this.determineNeighbor("up").isBorder()) {
      tlBrightness = 0;
      trBrightness = 0;
    }
    else {
      tlBrightness = this.determineNeighbor("upLeft").computeBrightness();
      trBrightness = this.determineNeighbor("upRight").computeBrightness();
    }

    double dlBrightness;
    double drBrightness;

    if (this.determineNeighbor("down").isBorder()) {
      dlBrightness = 0;
      drBrightness = 0;
    }
    else {
      dlBrightness = this.determineNeighbor("downLeft").computeBrightness();
      drBrightness = this.determineNeighbor("downRight").computeBrightness();
    }

    double lBrightness = this.determineNeighbor("left").computeBrightness();
    double rBrightness = this.determineNeighbor("right").computeBrightness();

    return (tlBrightness + 2 * lBrightness + dlBrightness)
        - (trBrightness + 2 * rBrightness + drBrightness);

  }

  // Determines if this Pixel is a Border pixel
  public boolean isBorder() {
    return false;
  }


  //determines where the given IPixel is in relation to this
  public int determineParentIdx(IPixel that) {
    int idx = 0;
    
    if (that == null) { 
      System.out.println(idx);
      return idx;
    } 
    
    if (this.determineNeighbor("up").sameAs(that)
        || this.determineNeighbor("left").sameAs(that)) {
      idx = 0;
    } else if (this.determineNeighbor("upLeft").sameAs(that)) {
      idx = -1;
    } else if (this.determineNeighbor("upRight").sameAs(that)
        || (this.determineNeighbor("downLeft").sameAs(that))) {
      idx = 1; 
    } else {
      throw new RuntimeException("not a valid parent");
    }
 
    return idx;
  }

  public void removeSelf(IPixel that, boolean isVert) {
    int parentIdx;
    
    if (that == null) {
      parentIdx = 0;
    }
    else {
      parentIdx = this.determineParentIdx(that);
    }
    
    if (isVert) {
      this.left.updateNeighbor("right", this.right, true);
      if (parentIdx == 1) {
        this.right.updateNeighbor("up", this.up, true);
      }
      if (parentIdx == -1) {
        this.left.updateNeighbor("up", this.up, true);
      }
    }
    //when erroring, none of the neighbors are correct.
    else {
      this.up.updateNeighbor("down", this.down, true);
      //down left
      if (parentIdx == 1) {
        this.down.updateNeighbor("left", this.left, true);
      }
      //up left
      if (parentIdx == -1) {
        this.up.updateNeighbor("left", this.left, true);
      }
    }
  }
  
  //updates how this Pixel renders using the given color
  public void updateRenderColor(Color color) {
    this.renderColor = color;
  }
  
  // EFFECT: reinserts this Pixel by re-linking its neighbors 
  public void reinsertSelf(boolean isVert) {
    this.right.updateNeighbor("left", this, isVert);
    this.left.updateNeighbor("right", this, isVert);
    this.up.updateNeighbor("down", this, isVert);
    this.down.updateNeighbor("up", this, isVert);
  }
  
  // EFFECT: resets the render color of this Border
  public void resetRenderColor() {
    this.renderColor = this.color;
  }
}

// Represents the information for a seam, including where it came from, and it's totalWeight
class SeamInfo {
  Pixel pixel;
  double totalWeight;
  SeamInfo cameFrom;
  boolean isVert;

  // Base Constructor -> isVert is assumed to be true
  SeamInfo(Pixel pixel, SeamInfo cameFrom) {
    this.pixel = pixel;
    this.cameFrom = cameFrom;
    this.isVert = true;
  }
  
  SeamInfo(Pixel pixel, double totalWeight, SeamInfo cameFrom, boolean isVert) {
    this.pixel = pixel;
    this.totalWeight = totalWeight;
    this.cameFrom = cameFrom;
    this.isVert = isVert;
  }
  
  // Convenience Constructor for when totalWeight is not necessarily needed
  SeamInfo(Pixel pixel, SeamInfo cameFrom, boolean isVert) {
    this.pixel = pixel;
    this.cameFrom = cameFrom;
    this.isVert = isVert;
  }

  // EFFECT: removes this SeamInfo by relinking all of the neighbors of the nodes
  // present in this SeamInfo
  void removeSelf() {
    if (this.cameFrom == null) {
      this.pixel.removeSelf(null, this.isVert);
    }
    else {
      this.pixel.removeSelf(this.cameFrom.pixel, this.isVert);
    }

    // Check that there are more seams to remove
    if (this.cameFrom != null) {
      this.cameFrom.removeSelf();
    }
  }

  // Determines if the given Pixel, which represents the topLeft pixel
  // of the grid, is the first Pixel in this SeamInfo
  boolean containsCurrTopLeft(Pixel currTL) {
    return (this.cameFrom == null && this.pixel == currTL)
        || (this.cameFrom != null && cameFrom.containsCurrTopLeft(currTL));
  }
  
  // EFFECT: undoes this SeamInfo
  void undo() {
    if (this.cameFrom == null) {
      this.pixel.reinsertSelf(this.isVert);
    } else {
      this.pixel.reinsertSelf(this.isVert);
    }
   
    // resets the render color of the Pixel contained in this SeamInfo
    this.pixel.resetRenderColor();
    
    // Check that there are more seams to add back in
    if (this.cameFrom != null) {
      this.cameFrom.undo();
    }
  }
  
  // EFFECT: resets the render color of this entire seam
  void resetSelfRenderColor() {
    this.pixel.resetRenderColor();
    
    if (this.cameFrom != null) {
      this.cameFrom.resetSelfRenderColor();
    }
  } 
}

// Represents a Grid of Pixels
class Grid {
  Pixel topLeft;
  History history;
  int width;
  int height;

  Grid(FromFileImage i) {
    ArrayList<ArrayList<Pixel>> unlinkedGrid = new GridUtils().convertToPixel(i);
    this.width = unlinkedGrid.get(0).size();
    this.height = unlinkedGrid.size();
    this.topLeft = new GridUtils().convertToTopLeft(unlinkedGrid);
    this.history = new History();
  }

  // Design choice : one of our design choices is to create the new rows rather
  // than storing the initial rows for a few reasons : overall, the time complexity is
  // the same, and so is memory efficiency. However, this allows us to ensure there is no
  // "wrong data" and prevents possible invariance
  SeamInfo findMinVertSeam() {
    return new GridUtils().findMinSeam(this.findVertSeams(true));

  }
  
  // Finds all seams in this Grid, so the minimum seam can later be found. 
  ArrayList<SeamInfo> findVertSeams(boolean isVert) {
    GridUtils gu = new GridUtils();
    String direction = "right";
    String progression = "down";
    
    if (!isVert) {
      direction = "down";
      progression = "right";
    }
    
    
    ArrayList<Pixel> currentPixelRow = gu.getNext(this.topLeft, direction); 
    ArrayList<Double> pastCumulativeBrightness = gu.firstBrightness(currentPixelRow); 
    ArrayList<SeamInfo> pastSeams = gu.createFirstSeams(currentPixelRow, isVert); 

    // creates new data, populates it using the inner for loop, and updates the
    // past data to match.
    // we don't care about the row counter, we want the benefits of a while loop
    // while still being able to modify the termination condition (and without
    // iterators)
    currentPixelRow = gu.getNext(currentPixelRow.get(0).determineNeighbor(progression), direction);
    if (currentPixelRow.get(0).isBorder()) {
      return pastSeams;
    }
    
    int refRowSize = currentPixelRow.size();
    
    // Accumulates all of the possible seams (given that we are trying to find the minimum
    // horizontal or vertical seams) into the currentSeamInfo ArrayList
    // NOTE: row is never actually used, it is simply so we can iterate through a grid-like
    // structure. It's value, however, does not matter.
    for (int row = 0; refRowSize != 0; row += 1) {
      ArrayList<SeamInfo> currentSeamInfo = new ArrayList<>();
      ArrayList<Double> currentCumulativeBrightness = new ArrayList<>();
      
      for (int col = 0; col < refRowSize; col += 1) {
        int minParentIdx = gu.findMinBrightParent(pastCumulativeBrightness, col);
        currentCumulativeBrightness.add(
            currentPixelRow.get(col).computeEnergy() + pastCumulativeBrightness.get(minParentIdx));

        currentSeamInfo.add(
            new SeamInfo(currentPixelRow.get(col),
                currentCumulativeBrightness.get(col), 
                pastSeams.get(minParentIdx),
                pastSeams.get(minParentIdx).isVert));
      }

      pastSeams = currentSeamInfo;
      pastCumulativeBrightness = currentCumulativeBrightness;
      
      if (currentPixelRow.get(0).determineNeighbor(progression).isBorder()) {
        break;
      }
      
      currentPixelRow = 
          gu.getNext(currentPixelRow.get(0).determineNeighbor(progression), direction);
    }

    return pastSeams;
  }

  // Finds the minimum horizontal seam of the found seams
  SeamInfo findMinHorizSeam() {
    return new GridUtils().findMinSeam(this.findVertSeams(false));
    // essentially make a getNextCol instead of getNext row, everything else should
    // be the same;
  }

  // EFFECT: removes the given seam from this grid.
  void removeSeam(SeamInfo seam) {
    history.add(seam);
    
    boolean seamVert = seam.isVert;
    
    seam.removeSelf();
    
    if (seam.containsCurrTopLeft(topLeft)) {
      if (seamVert) {
        this.topLeft = (Pixel) topLeft.right;
      } else {
        this.topLeft = (Pixel) topLeft.down;
      }
    }
    
    if (seamVert) {
      width -= 1;
    } else {
      height -= 1;
    }
  }
  
  // EFFECT: undoes the previous seam removal from this grid
  void undo() {
    if (this.hasHistory()) {
      SeamInfo toUndo = history.pop();
      toUndo.undo();
      
      if (toUndo.isVert) {
        width += 1;
      } else {
        height += 1;
      }
    } 
  }
  
  // Does this Grid have any history?
  boolean hasHistory() {
    return !this.history.isEmpty(); 
  }

  // Renders this Grid
  ComputedPixelImage render() {
    GridUtils utils = new GridUtils();
    ComputedPixelImage result = new ComputedPixelImage((int) this.width, (int) this.height);
    IPixel currTopLeft = this.topLeft;

    // Goes through each Pixel of the ComputedPixelImage result and sets the pixel
    // at a given
    // (x, y) position to its appropriate Color
    for (int rowIdx = 0; rowIdx < height; rowIdx += 1) {
      ArrayList<Pixel> currRow = utils.getNext(currTopLeft, "right");
      for (int colIdx = 0; colIdx < width; colIdx += 1) {
        result.setColorAt(colIdx, rowIdx, currRow.get(colIdx).renderColor);
      }

      currTopLeft = currTopLeft.determineNeighbor("down");
    }

    return result;
  }
 
  // Renders this Seam dependent on its Energy
  ComputedPixelImage renderEnergy() {
    GridUtils utils = new GridUtils();
    ComputedPixelImage result = new ComputedPixelImage((int) this.width, (int) this.height);
    IPixel currTopLeft = this.topLeft;

    // Goes through each Pixel of the ComputedPixelImage result and sets the pixel
    // at a given
    // (x, y) position to its appropriate Color
    for (int rowIdx = 0; rowIdx < height; rowIdx += 1) {
      ArrayList<Pixel> currRow = utils.getNext(currTopLeft, "right");
      for (int colIdx = 0; colIdx < width; colIdx += 1) {
        //reason for 18 : the max brightness is 3, and the max possible energy 
        //is sqrt(144 * 2), which is 16.97.... which can be rounded to 17; 
        Pixel currPixel = currRow.get(colIdx);
        if (currPixel.renderColor == currPixel.color) {
          double energyColor = (currRow.get(colIdx).computeEnergy() / 17);
          energyColor = energyColor * 255;
          result.setColorAt(
              colIdx,
              rowIdx,
              new Color((int)energyColor, (int)energyColor, (int)energyColor));
        }
        else {
          result.setColorAt(colIdx, rowIdx, currPixel.renderColor);
        }
      }

      currTopLeft = currTopLeft.determineNeighbor("down");
    }

    return result;
  }
  
  // paints the pixels in the given seam red
  void paintSeamRed(SeamInfo seam) {
    seam.pixel.updateRenderColor(Color.RED);
    
    if (seam.cameFrom != null) {
      this.paintSeamRed(seam.cameFrom);
    }
  }
}

// Represents a simulation of cutting down an Image
class ImageSimulation extends World {
  Grid img;
  FromFileImage initImage;
  boolean isPaused;
  boolean isRand;
  boolean isVert;
  //0 mode : default, 1 is energy, 2 is weight
  int mode;
  int tick;
  SeamInfo recentSeam;

  ImageSimulation(FromFileImage img) {
    this.initImage = img;
    this.img = new Grid(img);
    this.isPaused = false;
    this.isVert = false;
    this.tick = 0;
    //on step 0, find and paint seam. on step 2, remove seam
    this.mode = 0;
  }

  // EFFECT: will continue to remove seams from this Grid img so long as
  // the user has not paused the simulation.
  public void onTick() {
    if (isRand) {
      isVert = Math.random() > 0.5;
    }
    if (!isPaused) {
      if (tick % 2 == 0) {
        if (isVert) {
          recentSeam = img.findMinVertSeam();
        }
        else {
          recentSeam = img.findMinHorizSeam();
        }
        img.paintSeamRed(recentSeam);
      }
      else if (tick % 2 == 1) {
        img.removeSeam(recentSeam);
      } 
      
      tick += 1;
    }
  }

  // Changes the state of this ImageSimulation depending on the key that is pressed.
  public void onKeyEvent(String key) {
    //design decision : if a user chooses a direction,
    //the renderer is locked in that direction
    //if the user chooses the same direction again
    //the simulation chooses seams at random
    if (key.equals(" ")) {
      isPaused = !isPaused;
    }
    if (key.equals("v")) {
      this.isVert = true;
      this.isRand = false;
    }
    if (key.equals("h")) {
      this.isVert = false;
      this.isRand = false;
    }
    if (key.equals("q")) {
      this.mode = 0;
    }
    if (key.equals("r")) {
      this.isRand = true;
    }
    if (key.equals("w")) {
      this.mode = 1;
    }

    if (key.equals("u")) {
      if (this.img.hasHistory()) {
        this.recentSeam.resetSelfRenderColor();
        this.tick = 0;      
        this.img.undo();
      } 
    }
  }

  // Makes the rendered image of the grid contained in this ImageSimulation.
  public WorldImage makeImage() {
    if (mode != 1) {
      return this.img.render();
    }
    else if (mode == 1) {
      return this.img.renderEnergy();
    }
    else {
      throw new IllegalArgumentException("mode is invalid");
    }
  }

  // Makes the scene for this ImageSimulation
  public WorldScene makeScene() {
    WorldScene canvas = new WorldScene((int) initImage.getWidth(), (int) initImage.getHeight());
    canvas.placeImageXY(this.makeImage(), (int) initImage.getWidth() / 2,
        (int) initImage.getHeight() / 2);
    return canvas;
  }

  // Determines if this ImageSimulation should end
  public boolean shouldWorldEnd() {
    //design choice : a image should always have at least one pixel in one direction
    return this.img.width == 1 || this.img.height == 1;
  }

  // Draws the lastScene of this ImageSimulation after it has been determined that
  // this ImageSimulation should end.
  public WorldScene lastScene(String msg) {
    WorldScene canvas = new WorldScene((int) initImage.getWidth(), (int) initImage.getHeight());
    canvas.placeImageXY(this.makeImage(), (int) initImage.getWidth() / 2,
        (int) initImage.getHeight() / 2);
    return canvas;
  }
}

//Holds methods that are important for the Grid class (whether it be during construction
//or a method) that do not require the "this" keyword.
class GridUtils {
  
  // Gets the next row given the provided topleft IPixel and the direction.
  //if the next row is vertical, it returns a transposed version
  ArrayList<Pixel> getNext(IPixel topLeft, String direction) {
    ArrayList<Pixel> nextRow = new ArrayList<>();
    IPixel currPixel = topLeft;

    while (!currPixel.isBorder()) {
      // INVARIENT: each pixel added to the ArrayList will always be a Pixel and not a
      // Border
      // due to the nature of the loop
      nextRow.add((Pixel) currPixel);

      currPixel = currPixel.determineNeighbor(direction);
    }

    return nextRow;
  }

  // Finds the brightness of the given list of Pixels
  public ArrayList<Double> firstBrightness(ArrayList<Pixel> currentPixelRow) {
    ArrayList<Double> brightnesses = new ArrayList<>();
    for (Pixel p : currentPixelRow) {
      brightnesses.add(p.computeBrightness());
    }
    return brightnesses;
  }

  // using the given list of pixels, creates the starting point for seams
  ArrayList<SeamInfo> createFirstSeams(ArrayList<Pixel> arrList, boolean isVert) {
    ArrayList<SeamInfo> firstSeams = new ArrayList<>();
    
    for (Pixel p : arrList) {
      firstSeams.add(new SeamInfo(p, null, isVert));
    }
    
    return firstSeams;
  }

  // Finds the seam with the minimum value using the given list of Seams
  SeamInfo findMinSeam(ArrayList<SeamInfo> seams) {
    SeamInfo minSeam = seams.get(0);
    // goes through each index of minSeams and determines if it is smaller (by total
    // weight) than
    // the current smallest (by total weight) seam.
    for (int index = 0; index < seams.size(); index += 1) {
      if (minSeam.totalWeight > seams.get(index).totalWeight) {
        minSeam = seams.get(index);
      }
    }
    return minSeam;
  }

  // given the past cumulative brightness values, as well as where the
  // current cell is in the row, finds the index with the least brightness
  int findMinBrightParent(ArrayList<Double> pastBrightness, int col) {

    ArrayList<Double> parents = new ArrayList<>();

    // Generate the parent array of brightness so we can later find the maximum
    // brightness among these
    if (col != 0) {
      parents.add(pastBrightness.get(col - 1));
    }
    else {
      parents.add(Double.MAX_VALUE);
    }

    parents.add(pastBrightness.get(col));

    if (col != pastBrightness.size() - 1) {
      parents.add(pastBrightness.get(col + 1));
    }
    else {
      parents.add(Double.MAX_VALUE);
    }

    // Determine min brightness among the selected parents
    double minParent = Math.min(parents.get(0), parents.get(1));

    // represents the column distance the minimum parent is from the input
    // parameter, col
    int minParentIdx;

    if (minParent == parents.get(1)) {
      minParentIdx = 0;
    }
    else if (Math.min(minParent, parents.get(2)) == parents.get(2)) {
      minParentIdx = 1;
    }
    else {
      minParentIdx = -1;
    }

    return col + minParentIdx;
  }

  // Converts the given Image into the top-left most Pixel in the given image,
  // where each subsequent
  // Pixel is appropriately linked to all of its neighbors (top, bottom, left,
  // right)
  Pixel convertToTopLeft(FromFileImage i) {
    return this.linkNeighbors(this.convertToPixel(i));
  }

  // overload operator if we already know what the unlinked grid is
  Pixel convertToTopLeft(ArrayList<ArrayList<Pixel>> unlinkedGrid) {
    return this.linkNeighbors(unlinkedGrid);
  }

  // Converts the given Image into an ArrayList<ArrayList<Pixel>> objects
  // to represent a grid of Pixels for ease of traversal
  ArrayList<ArrayList<Pixel>> convertToPixel(FromFileImage img) {
    ArrayList<ArrayList<Pixel>> pixelGrid = new ArrayList<>();

    double width = img.getWidth();
    double height = img.getHeight();

    // Go through each row of of the image to initialize the pixels
    for (int y = 0; y < height; y += 1) {
      ArrayList<Pixel> row = new ArrayList<>();
      // go through each column of the image to initialize the pixels
      for (int x = 0; x < width; x += 1) {
        Color color = img.getColorAt(x, y);

        // Add the current Pixel to the end of the current row
        row.add(new Pixel(color));
      }

      // Add the current row to the end of the grid of pixels
      pixelGrid.add(row);
    }

    return pixelGrid;
  }

  // Links all of the Pixels in the given nested list of Pixels to their
  // respective
  // up, down, left, and right neighbors and returns the topLeft pixel (at index
  // (0,0))
  // once all of the Pixels have been properly linked to each other.
  Pixel linkNeighbors(ArrayList<ArrayList<Pixel>> grid) {
    if (grid.size() == 0) {
      throw new IllegalArgumentException("Cannot find a topLeft pixel of an empty grid");
    }

    // It can now be assumed that there are at least one row of pixels in the
    // provided grid
    int numCol = grid.get(0).size();
    int numRow = grid.size();

    // EFFECT: Go through each Pixel (accessed via indexing the grid) and set their
    // neighbors to the appropriate neighbor (top, bottom, left, and right)
    for (int row = 0; row < grid.size(); row += 1) {
      for (int col = 0; col < numCol; col += 1) {
        IPixel currPixel = grid.get(row).get(col);
        IPixel left = null;
        IPixel right = null;
        IPixel up = null;
        IPixel down = null;
        // Update the edge pixel
        if (col == 0) {
          left = new Border();
        }
        else if (col == numCol - 1) {
          right = new Border();
        }
        if (row == 0) {
          up = new Border();
        }
        else if (row == numRow - 1) {
          down = new Border();
        }
        if (left == null) {
          left = grid.get(row).get(col - 1);
        }
        if (right == null) {
          right = grid.get(row).get(col + 1);
        }
        if (up == null) {
          up = grid.get(row - 1).get(col);
        }
        if (down == null) {
          down = grid.get(row + 1).get(col);
        }
        currPixel.updateNeighbor(up, down, left, right);
      }
    }
    return grid.get(0).get(0);
  } 
}