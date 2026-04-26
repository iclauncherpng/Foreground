package mindustry.ui.layout;

import arc.math.*;
import arc.struct.*;

public class RadialTreeLayout implements TreeLayout {
    private ObjectSet<TreeNode> visited = new ObjectSet<>();
    private Queue<TreeNode> queue = new Queue<>();

    public float startRadius = 160f;
    public float delta = 160f;

    @Override
    public void layout(TreeNode root) {
        if(root == null) return;

        root.x = 0;
        root.y = 0;

        bfs(root, true);

        Seq<TreeNode> all = new Seq<>();
        findAll(root, all);
        for(TreeNode node : all){
            node.leaves = bfs(node, false);
        }

        radialize(root, startRadius, 0, 360);
    }

    private void radialize(TreeNode root, float radius, float from, float to){
        float angle = from;

        for(TreeNode child : root.children){
            float sectorSize = (float)child.leaves / root.leaves * (to - from);
            float midAngle = angle + sectorSize / 2f;

            child.x = radius * Mathf.cosDeg(midAngle);
            child.y = radius * Mathf.sinDeg(midAngle);

            if(child.children.length > 0){
                radialize(child, radius + delta, angle, angle + sectorSize);
            }

            angle += sectorSize;
        }
    }

    private void findAll(TreeNode node, Seq<TreeNode> out){
        out.add(node);
        for(TreeNode child : node.children) findAll(child, out);
    }

    private int bfs(TreeNode node, boolean assign){
        visited.clear();
        queue.clear();
        int leaves = 0;

        visited.add(node);
        queue.addLast(node);

        while(!queue.isEmpty()){
            TreeNode current = queue.removeFirst();
            if(current.children.length == 0) leaves++;

            for(TreeNode child : current.children){
                if(assign) child.number = current.number + 1;
                if(visited.add(child)){
                    queue.addLast(child);
                }
            }
        }
        return leaves;
    }
}