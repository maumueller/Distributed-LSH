package hashfunctions

import measures.Distance

import scala.util.Random

case class Hyperplane(k: Int, seed:Long, numOfDim: Int) extends HashFunction {
  private val rnd:Random = new Random(seed)
  private val numberOfDimensions:Int = numOfDim
  private val hyperPlanes = for {
    _ <- (0 until k).toArray
    hp <- Array(generateRandomV(numberOfDimensions))
  } yield hp

  private val probes:Array[Array[Int]] = {
    val a = new Array[Array[Int]]((k*(k+1)/2)+1) // Array of probes to be reused
    for(i <- 0 until a.length) {
      a(i) =  new Array[Int](k)
    }
    a
  }


  override def apply(v: Array[Float]): Array[Int] = {
    val result = new Array[Int](k)
    // For performance, a while loop is used
    var i = 0
    while(i < k) {
      result(i) = hash(v, hyperPlanes(i))
      i+=1
    }
    result
  }

  // TODO Change this into Breeze dotproduct
  // TODO Remove the branch if possible
  private def hash(v: Array[Float], randomV: Array[Float]): Int = {
    if (Distance.dotProduct(v, randomV) > 0) 1 else 0
  }

  /**
    * Generates random hyperplane
    * @param size
    * @return random hyperplane
    */
  def generateRandomV(size:Int) : Array[Float] = {
    for {
      _ <- (0 until size).toArray
      c <- Array[Float]({
        if (rnd.nextBoolean()) -1 else 1
      })
    } yield c
  }

  /**
    * 2-step multiprobe scheme
    * Generates a set of keys from a vector
    *
    * @param hashCode
    * @return
    */

  override def generateProbes(hashCode: Array[Int]): Array[Array[Int]] = {

    var i,j,c = 0

    // Insert query element
    System.arraycopy(hashCode, 0, probes(c), 0, k)
    c+=1

    // Generate buckets
    while(i < k) {
      System.arraycopy(hashCode, 0, probes(c), 0, k) // Copies values from hashCode into existing array in probes
      // TODO remove this assignment if possible
      probes(c)(i) = 1 - probes(c)(i)// efficient flip (here we permute)
      val OneStepProbe = probes(c)

      c = c+1 // c is updated to copy the reference into probes array index
      j = i+1
      while(j < k) {
        System.arraycopy(OneStepProbe, 0, probes(c), 0, k)
        // using i'th permute to generate the j set
        probes(c)(j) = 1 - probes(c)(j)
        c = c+1
        j = j+1
      }
      i+=1
    }
    this.probes // probes has been update in place
  }
}
