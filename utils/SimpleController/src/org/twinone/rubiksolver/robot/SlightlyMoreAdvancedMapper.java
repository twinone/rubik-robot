package org.twinone.rubiksolver.robot;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import org.twinone.rubiksolver.robot.SimpleRobotMapper.RequestTag;
import org.twinone.rubiksolver.robot.comm.DelayRequest;
import org.twinone.rubiksolver.robot.comm.Request;

/**
 * This mapper tries to minimize cube rotations, and parallelizes
 * face rotations. This is dirty code, somehow temporary.
 */
public class SlightlyMoreAdvancedMapper {
    
    public static int getAxis(int[] a) {
        for (int i = 0;; i++)
            if (a[i] != 0) return i;
    }
    public static int[] cross(int[] a, int[] b) {
        return new int[]{a[1]*b[2] - a[2]*b[1], a[2]*b[0] - a[0]*b[2], a[0]*b[1] - a[1]*b[0]};
    }
    public static int[] rotateBy(int[] a, int[] rotation) {
        if (getAxis(rotation) == getAxis(a)) return a;
        int turns = Math.abs(rotation[0] + rotation[1] + rotation[2]);
        rotation = new int[]{(int)Math.signum(rotation[0]), (int)Math.signum(rotation[1]), (int)Math.signum(rotation[2])};
        for (int i = 0; i < turns; i++) a = cross(a, rotation);
        return a;
    }
    public static class CubeOrientation {
        final int[] R;
        final int[] U;
        final int[] F;
        final int[][] axis;

        private CubeOrientation(int[] R, int[] U, int[] F) {
            this.R = R;
            this.U = U;
            this.F = F;
            this.axis = new int[][]{R, U, F};
        }
        
        public static final CubeOrientation RESET = new CubeOrientation(new int[]{1, 0, 0}, new int[]{0, 0, 1}, new int[]{0, -1, 0});
        
        public CubeOrientation rotate(int[] rot) {
            return new CubeOrientation(rotateBy(R, rot), rotateBy(U, rot), rotateBy(F, rot));
        }
        
        public int[] getFace(char face) {
            int idx = "RLUDFB".indexOf(face);
            if (idx == -1) return null;
            int[] v = axis[idx/2];
            if (idx % 2 != 0) v = new int[] {-v[0], -v[1], -v[2]};
            return v;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 19 * hash + Arrays.deepHashCode(this.axis);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CubeOrientation other = (CubeOrientation) obj;
            return Arrays.deepEquals(this.axis, other.axis);
        }
        
        public int[][] findOptimalPath(CubeOrientation target, int depth) {
            if (this.equals(target)) return new int[0][];
            int[][] bestCandidate = null;
            if (depth > 0) {
                for (int axis = 0; axis < 2; axis++)
                for (int turn : new int[]{ -1, +1 }) {
                    int[] rotation = new int[3];
                    rotation[axis] = turn;
                    int[][] candidate = findOptimalPath(target, depth - 1);
                    if (candidate != null && (bestCandidate == null || candidate.length+1 < bestCandidate.length)) {
                        bestCandidate = new int[candidate.length+1][];
                        bestCandidate[0] = rotation;
                        System.arraycopy(candidate, 0, bestCandidate, 1, candidate.length);
                        if (candidate.length == 0) break;
                    }
                }
            }
            return bestCandidate;
        }
    }
    
    public static class Chunk {
        int axis; // real axis managed by this chunk
        int positiveSideTurns;
        int negativeSideTurns;
    }
    public static void processMoves(List<Chunk> result, Iterator<AlgorithmMove> moves) {
        List<Chunk> chunks = new ArrayList<>();
        CubeOrientation orientation = CubeOrientation.RESET;
        while (moves.hasNext()) {
            AlgorithmMove move = moves.next();
            
            // Axis rotation: just change reference
            int axis = "XYZ".indexOf(move.face);
            if (axis != -1) {
                int[] rotation = new int[3];
                rotation[axis] = move.reverse ? -1 : +1;
                orientation = orientation.rotate(rotation);
                continue;
            }
            
            // Face moves
            int idx = "RLUDFB".indexOf(move.face);
            if (idx != -1) {
                if (chunks.isEmpty() || chunks.get(chunks.size()-1).axis != idx/2)
                    chunks.add(new Chunk());
                Chunk c = chunks.get(chunks.size()-1);
                c.axis = idx/2;
                int turn = move.reverse ? -1 : +1;
                if (idx % 2 == 0) c.positiveSideTurns += turn;
                else c.negativeSideTurns += turn;
                continue;
            }
            
            throw new IllegalArgumentException("Dafuq is this");
        }
        for (Chunk chunk : chunks)
            if (chunk.positiveSideTurns != 0 || chunk.negativeSideTurns != 0)
                result.add(chunk);
    }
    
    public static class DoubleChunk {
        Set<Integer> axis;
        List<Chunk> chunks;
    }
    
    public static void processChunks(List<DoubleChunk> result, List<Chunk> chunks) {
        for (Chunk chunk : chunks) {
            if (result.isEmpty() || (result.get(result.size()-1).axis.size() >= 2 && !result.get(result.size()-1).axis.contains(chunk.axis))) {
                DoubleChunk place = new DoubleChunk();
                place.axis = new HashSet<>();
                place.chunks = new ArrayList<>();
                result.add(place);
            }
            DoubleChunk place = result.get(result.size()-1);
            place.axis.add((Integer)chunk.axis);
            place.chunks.add(chunk);
        }
    }
    
    public CubeOrientation orientation = CubeOrientation.RESET;
    public SimpleRobotMapper mapper = new SimpleRobotMapper();
    public Queue<Request> requests = new ArrayDeque<>();
    
    public List<Request> executeTurns(List<RequestTag> tags, int side, int turns) {
        List<Request> result = new ArrayList<>();
        char face = "RBLF".charAt(side % 4);
        turns = ((turns % 4) + 4) % 4;
        if (turns == 1) {
            mapper.map(result, tags, new AlgorithmMove(face, false));
        } else if (turns == 2) {
            mapper.map(result, tags, new AlgorithmMove(face, false));
            mapper.map(result, tags, new AlgorithmMove(face, false));
        } else if (turns == 3) {
            mapper.map(result, tags, new AlgorithmMove(face, true));
        }
        return result;
    }
    
    public void rotateAxis(List<RequestTag> tags, boolean logicalAxis, boolean reverse) {
        List<Request> result = new ArrayList<>();
        mapper.map(result, tags, new AlgorithmMove(logicalAxis ? 'Z' : 'X', reverse));
        requests.addAll(result);
        
        int[] rotation = new int[3];
        rotation[logicalAxis ? 1 : 0] = reverse ? +1 : -1;
        orientation = orientation.rotate(rotation);
    }
    
    public void processDoubleChunks(List<RequestTag> tags, List<DoubleChunk> places) {
        for (DoubleChunk place : places) {
            
            // Ensure the axis in this place are visible
            if (place.axis.size() == 2) {
                Set<Integer> axis = new HashSet<>();
                Collections.addAll(axis, 0, 1, 2);
                axis.removeAll(place.axis);
                Integer otherAxis = axis.iterator().next();
                // If this axis is visible, make it the non-visible axis
                if (orientation.axis[otherAxis][2] == 0) {
                    boolean logicalAxis = orientation.axis[otherAxis][1] != 0;
                    rotateAxis(tags, !logicalAxis, false);
                }
            } else if (place.axis.size() == 1) {
                Integer axis = place.axis.iterator().next();
                if (orientation.axis[axis][2] != 0) {
                    // The axis is the non-visible one, any rotation will do
                    rotateAxis(tags, false, false);
                }
            }
            
            for (Chunk chunk : place.chunks) {
                int[] v = orientation.axis[chunk.axis];
                int side = getAxis(v) + v[0] + v[1] + 1 + 2;
                List<Request> positiveRequests = executeTurns(tags, side, chunk.positiveSideTurns);
                List<Request> negativeRequests = executeTurns(tags, side + 2, chunk.negativeSideTurns);
                parallelize(requests, positiveRequests.iterator(), negativeRequests.iterator());
            }
        }
    }
    
    public void resetOrientation(List<RequestTag> tags) {
        for (int[] rotation : orientation.findOptimalPath(CubeOrientation.RESET, 4))
            rotateAxis(tags, getAxis(rotation) == 1, (rotation[0] + rotation[1]) > 0);
    }
    
    public static void parallelize(Queue<Request> result, Iterator<Request>... requests) {
        class TimedRequest {
            int time;
            Request request;
        }
        
        List<TimedRequest> events = new ArrayList<>();
        int maxTime = 0;
        for (Iterator<Request> requestIterator : requests) {
            int time = 0;
            while (requestIterator.hasNext()) {
                Request r = requestIterator.next();
                if (r instanceof DelayRequest) {
                    time += ((DelayRequest)r).getDelay();
                    continue;
                }
                TimedRequest event = new TimedRequest();
                event.time = time;
                event.request = r;
                events.add(event);
            }
            if (maxTime < time) maxTime = time;
        }
        
        Collections.sort(events, new Comparator<TimedRequest>() {
            @Override
            public int compare(TimedRequest a, TimedRequest b) {
                return a.time - b.time;
            }
        });
        
        int time = 0;
        for (TimedRequest event : events) {
            if (event.time > time) {
                result.add(new DelayRequest((short) (event.time - time))); //FIXME
                time = event.time;
            }
            result.add(event.request);
        }
        if (maxTime > time) result.add(new DelayRequest((short) (maxTime - time))); //FIXME
    }
    
    public List<Request> map(List<AlgorithmMove> algorithm, boolean resetOrientation, List<RequestTag> tags) {
        List<Chunk> chunks = new ArrayList<>();
        SlightlyMoreAdvancedMapper.processMoves(chunks, algorithm.iterator());
        
        List<DoubleChunk> places = new ArrayList<>();
        SlightlyMoreAdvancedMapper.processChunks(places, chunks);
        
        processDoubleChunks(tags, places);
        if (resetOrientation) resetOrientation(tags);
        
        List<Request> result = new ArrayList<>();
        while (!requests.isEmpty())
            result.add(requests.remove());
        return result;
    }
    
}
